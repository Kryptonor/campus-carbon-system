// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.8.0;

/**
 * @title AuditTrail
 * @notice 校园碳积分审计存证合约 — 不可篡改的行为记录与兑换记录上链存证
 *
 * @dev 设计决策:
 *  - 仅存业务摘要（imageHash、行为类型、积分、判定结果等），不存图片本身
 *  - 每条记录与链下 MySQL 主键 ID 一一对应，便于联合查询
 *  - 同一 imageHash 不可重复上链，防止图片复用刷分
 *  - 仅合约管理员可写入；任何人可查询
 *
 * 存证的两类记录:
 *  - BehaviorRecord: 用户的低碳行为（光盘行动/回收）及其 AI 判定结果
 *  - ExchangeRecord: 用户的商品兑换记录
 *
 * 使用场景:
 *  - recordBehavior:  AI 验证用户上传的图片后，后端将行为记录存证上链
 *  - recordExchange:  用户完成积分兑换后，后端将兑换记录存证上链
 *  - isImageHashExists: AI 验证前先检查图片是否已被提交过
 */
contract AuditTrail {


    /// @notice 行为存证记录
    struct BehaviorRecord {
        uint256 recordId;       // 对应 MySQL behavior_records.id
        string  studentNo;      // 学生学号
        string  behaviorType;   // 行为类型: "clean_plate" | "recycle"
        bytes32 imageHash;      // 图片 SHA-256 哈希值
        string  aiLabel;        // AI 识别返回的标签文本
        uint8   aiScore;        // AI 置信度分数 (0-100，已放大为整数存储)
        uint256 points;         // 本次行为获得的积分数
        string  decision;       // AI 判定结果: "PASS" | "REJECT"
        uint256 timestamp;      // 上链时间戳
    }

    /// @notice 兑换存证记录
    struct ExchangeRecord {
        uint256 recordId;       // 对应 MySQL exchange_records.id
        string  studentNo;      // 学生学号
        uint256 productId;      // 兑换商品 ID
        uint8   amount;         // 兑换数量
        uint256 totalPoints;    // 消耗总积分
        bytes32 redeemCodeHash; // 核销码的 keccak256 哈希
        uint256 timestamp;      // 上链时间戳
    }



    /// @notice 合约管理员地址（后端服务账户）
    address public admin;

    /// @notice 行为记录 ID → 存证数据
    mapping(uint256 => BehaviorRecord) private _behaviorRecords;

    /// @notice 兑换记录 ID → 存证数据
    mapping(uint256 => ExchangeRecord) private _exchangeRecords;

    /// @notice 学号 → 该用户的行为记录 ID 列表
    mapping(string => uint256[]) private _userBehaviorIds;

    /// @notice 学号 → 该用户的兑换记录 ID 列表
    mapping(string => uint256[]) private _userExchangeIds;

    /// @notice 已上链的图片哈希集合（防重复提交）
    mapping(bytes32 => bool) private _imageHashes;

    /// @notice 行为存证总数
    uint256 public behaviorCount;

    /// @notice 兑换存证总数
    uint256 public exchangeCount;



    /// @notice 行为记录存证事件
    event BehaviorRecorded(
        uint256 indexed recordId,
        string indexed studentNo,
        string  behaviorType,
        bytes32 imageHash,
        uint256 points,
        string decision,
        uint256 timestamp
    );

    /// @notice 兑换记录存证事件
    event ExchangeRecorded(
        uint256 indexed recordId,
        string indexed studentNo,
        uint256 productId,
        uint256 totalPoints,
        uint256 timestamp
    );

    /// @notice 管理员变更事件
    event AdminChanged(
        address indexed oldAdmin,
        address indexed newAdmin
    );



    /// @param initialAdmin 初始管理员地址
    constructor(address initialAdmin) {
        require(initialAdmin != address(0), "AuditTrail: zero admin address");
        admin = initialAdmin;
    }



    /// @notice 将行为记录存证上链
    /// @dev 仅管理员可调用；同一 recordId 不可重复写入；同一 imageHash 不可重复提交
    /// @param recordId     链下 MySQL 中的行为记录主键 ID
    /// @param studentNo    学生学号
    /// @param behaviorType 行为类型（"clean_plate" / "recycle"）
    /// @param imageHash    上传图片的 SHA-256 哈希（32 字节）
    /// @param aiLabel      AI 识别标签文本
    /// @param aiScore      AI 置信度（0-100 整数，后端已 ×100）
    /// @param points       本次获得的积分
    /// @param decision     AI 判定结果（"PASS" / "REJECT"）
    function recordBehavior(
        uint256 recordId,
        string  memory studentNo,
        string  memory behaviorType,
        bytes32 imageHash,
        string  memory aiLabel,
        uint8   aiScore,
        uint256 points,
        string  memory decision
    )
        external
        onlyAdmin
    {
        require(recordId > 0, "AuditTrail: invalid recordId");
        require(bytes(studentNo).length > 0, "AuditTrail: empty studentNo");
        require(bytes(behaviorType).length > 0, "AuditTrail: empty behaviorType");
        require(imageHash != bytes32(0), "AuditTrail: zero imageHash");
        require(
            _behaviorRecords[recordId].timestamp == 0,
            "AuditTrail: recordId already exists"
        );
        require(
            !_imageHashes[imageHash],
            "AuditTrail: duplicate image hash"
        );

        _behaviorRecords[recordId] = BehaviorRecord({
            recordId:    recordId,
            studentNo:   studentNo,
            behaviorType: behaviorType,
            imageHash:   imageHash,
            aiLabel:     aiLabel,
            aiScore:     aiScore,
            points:      points,
            decision:    decision,
            timestamp:   block.timestamp
        });

        _imageHashes[imageHash] = true;
        _userBehaviorIds[studentNo].push(recordId);
        behaviorCount++;

        emit BehaviorRecorded(
            recordId, studentNo, behaviorType, imageHash,
            points, decision, block.timestamp
        );
    }

    /// @notice 将兑换记录存证上链
    /// @dev 仅管理员可调用；同一 recordId 不可重复写入
    /// @param recordId       链下 MySQL 中的兑换记录主键 ID
    /// @param studentNo      学生学号
    /// @param productId      兑换的商品 ID
    /// @param amount         兑换数量
    /// @param totalPoints    消耗总积分
    /// @param redeemCodeHash 核销码的 keccak256 哈希
    function recordExchange(
        uint256 recordId,
        string  memory studentNo,
        uint256 productId,
        uint8   amount,
        uint256 totalPoints,
        bytes32 redeemCodeHash
    )
        external
        onlyAdmin
    {
        require(recordId > 0, "AuditTrail: invalid recordId");
        require(bytes(studentNo).length > 0, "AuditTrail: empty studentNo");
        require(amount > 0, "AuditTrail: amount must be positive");
        require(totalPoints > 0, "AuditTrail: totalPoints must be positive");
        require(
            _exchangeRecords[recordId].timestamp == 0,
            "AuditTrail: recordId already exists"
        );

        _exchangeRecords[recordId] = ExchangeRecord({
            recordId:        recordId,
            studentNo:       studentNo,
            productId:       productId,
            amount:          amount,
            totalPoints:     totalPoints,
            redeemCodeHash:  redeemCodeHash,
            timestamp:       block.timestamp
        });

        _userExchangeIds[studentNo].push(recordId);
        exchangeCount++;

        emit ExchangeRecorded(
            recordId, studentNo, productId, totalPoints, block.timestamp
        );
    }



    /// @notice 查询行为存证记录
    /// @param recordId 行为记录 ID
    /// @return BehaviorRecord 完整记录
    function getBehaviorRecord(uint256 recordId)
        external
        view
        returns (BehaviorRecord memory)
    {
        require(
            _behaviorRecords[recordId].timestamp != 0,
            "AuditTrail: behavior record not found"
        );
        return _behaviorRecords[recordId];
    }

    /// @notice 查询兑换存证记录
    /// @param recordId 兑换记录 ID
    /// @return ExchangeRecord 完整记录
    function getExchangeRecord(uint256 recordId)
        external
        view
        returns (ExchangeRecord memory)
    {
        require(
            _exchangeRecords[recordId].timestamp != 0,
            "AuditTrail: exchange record not found"
        );
        return _exchangeRecords[recordId];
    }

    /// @notice 查询用户的全部行为记录 ID
    /// @param studentNo 学生学号
    /// @return 行为记录 ID 数组
    function getUserBehaviorRecordIds(string memory studentNo)
        external
        view
        returns (uint256[] memory)
    {
        return _userBehaviorIds[studentNo];
    }

    /// @notice 查询用户的行为记录总数
    /// @param studentNo 学生学号
    /// @return 行为记录数量
    function getUserBehaviorCount(string memory studentNo)
        external
        view
        returns (uint256)
    {
        return _userBehaviorIds[studentNo].length;
    }

    /// @notice 分页查询用户的行为记录 ID
    /// @param studentNo 学生学号
    /// @param offset    偏移量
    /// @param limit     每页数量
    /// @return ids 行为记录 ID 数组
    /// @return total 该用户的行为记录总数
    function getUserBehaviorRecordIdsPaginated(
        string memory studentNo,
        uint256 offset,
        uint256 limit
    )
        external
        view
        returns (uint256[] memory ids, uint256 total)
    {
        uint256[] storage all = _userBehaviorIds[studentNo];
        total = all.length;

        if (offset >= total) {
            return (new uint256[](0), total);
        }

        uint256 end = offset + limit > total ? total : offset + limit;
        uint256 size = end - offset;
        ids = new uint256[](size);
        for (uint256 i = 0; i < size; i++) {
            ids[i] = all[offset + i];
        }
        return (ids, total);
    }

    /// @notice 查询用户的全部兑换记录 ID
    /// @param studentNo 学生学号
    /// @return 兑换记录 ID 数组
    function getUserExchangeRecordIds(string memory studentNo)
        external
        view
        returns (uint256[] memory)
    {
        return _userExchangeIds[studentNo];
    }

    /// @notice 查询用户的兑换记录总数
    /// @param studentNo 学生学号
    /// @return 兑换记录数量
    function getUserExchangeCount(string memory studentNo)
        external
        view
        returns (uint256)
    {
        return _userExchangeIds[studentNo].length;
    }

    /// @notice 分页查询用户的兑换记录 ID
    /// @param studentNo 学生学号
    /// @param offset    偏移量
    /// @param limit     每页数量
    /// @return ids 兑换记录 ID 数组
    /// @return total 该用户的兑换记录总数
    function getUserExchangeRecordIdsPaginated(
        string memory studentNo,
        uint256 offset,
        uint256 limit
    )
        external
        view
        returns (uint256[] memory ids, uint256 total)
    {
        uint256[] storage all = _userExchangeIds[studentNo];
        total = all.length;

        if (offset >= total) {
            return (new uint256[](0), total);
        }

        uint256 end = offset + limit > total ? total : offset + limit;
        uint256 size = end - offset;
        ids = new uint256[](size);
        for (uint256 i = 0; i < size; i++) {
            ids[i] = all[offset + i];
        }
        return (ids, total);
    }

    /// @notice 检查某个图片哈希是否已经上链
    /// @dev 用于提交图片前的前置检查，防重复提交
    /// @param imageHash 图片的 SHA-256 哈希
    /// @return true 表示图片已存在
    function isImageHashExists(bytes32 imageHash)
        external
        view
        returns (bool)
    {
        return _imageHashes[imageHash];
    }

    /// @notice 批量检查多个图片哈希是否已存在
    /// @param imageHashes 图片哈希数组
    /// @return 对应的布尔结果数组
    function batchIsImageHashExists(bytes32[] memory imageHashes)
        external
        view
        returns (bool[] memory)
    {
        bool[] memory results = new bool[](imageHashes.length);
        for (uint256 i = 0; i < imageHashes.length; i++) {
            results[i] = _imageHashes[imageHashes[i]];
        }
        return results;
    }



    /// @notice 转移管理员权限
    /// @param newAdmin 新管理员地址
    function changeAdmin(address newAdmin)
        external
        onlyAdmin
    {
        require(newAdmin != address(0), "AuditTrail: zero admin address");
        require(newAdmin != admin, "AuditTrail: same admin");
        address oldAdmin = admin;
        admin = newAdmin;
        emit AdminChanged(oldAdmin, newAdmin);
    }



    /// @dev 仅管理员可调用的修饰符
    modifier onlyAdmin() {
        require(msg.sender == admin, "AuditTrail: caller is not admin");
        _;
    }
}
