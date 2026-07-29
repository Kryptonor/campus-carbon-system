// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.8.0;

/**
 * @title CarbonPoints
 * @notice 校园碳积分合约 — 管理积分的铸造、消耗与查询
 *
 * @dev 设计决策:
 *  - 不使用 ERC-20 标准（碳积分不可自由转账、交易，避免投机行为）
 *  - 仅合约管理员可以铸造 (mint) 和消耗 (burn) 积分
 *  - 积分与学号绑定，而非区块链地址，降低新用户理解门槛
 *  - 所有写操作均触发事件，方便链下监听同步
 *
 * 使用场景:
 *  - mint:  用户上传低碳行为图片，AI 验证通过 → 后端调用 mint 发放积分
 *  - burn:  用户用积分兑换商品 → 后端调用 burn 扣除积分
 *  - balanceOf: 任何人可查询指定学号的积分余额
 */
contract CarbonPoints {


    /// @notice 学号 → 积分余额
    mapping(string => uint256) private _balances;

    /// @notice 合约管理员地址（后端服务账户，拥有 mint/burn 权限）
    address public admin;

    /// @notice 已铸造积分总量（审计用）
    uint256 public totalMinted;

    /// @notice 已消耗积分总量（审计用）
    uint256 public totalBurned;



    /// @notice 积分铸造事件
    /// @param studentNo 学生学号
    /// @param amount   铸造的积分数额
    /// @param behaviorId 对应的链下行为记录 ID
    /// @param timestamp 上链时间戳
    event Minted(
        string indexed studentNo,
        uint256 amount,
        string behaviorId,
        uint256 timestamp
    );

    /// @notice 积分消耗事件
    /// @param studentNo 学生学号
    /// @param amount   消耗的积分数额
    /// @param exchangeId 对应的链下兑换记录 ID
    /// @param timestamp 上链时间戳
    event Burned(
        string indexed studentNo,
        uint256 amount,
        string exchangeId,
        uint256 timestamp
    );

    /// @notice 管理员变更事件
    event AdminChanged(
        address indexed oldAdmin,
        address indexed newAdmin
    );



    /// @notice 部署时指定管理员地址
    /// @param initialAdmin 初始管理员（通常是后端服务账户的区块链地址）
    constructor(address initialAdmin) {
        require(initialAdmin != address(0), "CarbonPoints: zero admin address");
        admin = initialAdmin;
    }



    /// @notice 铸造积分（用户完成低碳行为后由后端调用）
    /// @dev 仅管理员可调用
    /// @param studentNo  学生学号
    /// @param amount     积分数额
    /// @param behaviorId 关联的行为记录 ID（如 "BH-20260001-0001"）
    function mint(
        string memory studentNo,
        uint256 amount,
        string memory behaviorId
    )
        external
        onlyAdmin
    {
        require(amount > 0, "CarbonPoints: amount must be positive");
        require(bytes(studentNo).length > 0, "CarbonPoints: student number is empty");

        _balances[studentNo] += amount;
        totalMinted += amount;

        emit Minted(studentNo, amount, behaviorId, block.timestamp);
    }

    /// @notice 消耗积分（用户兑换商品时由后端调用）
    /// @dev 仅管理员可调用，会检查余额是否充足
    /// @param studentNo  学生学号
    /// @param amount     消耗积分数额
    /// @param exchangeId 关联的兑换记录 ID（如 "EX-20260001-0001"）
    function burn(
        string memory studentNo,
        uint256 amount,
        string memory exchangeId
    )
        external
        onlyAdmin
    {
        require(amount > 0, "CarbonPoints: amount must be positive");
        require(
            _balances[studentNo] >= amount,
            "CarbonPoints: insufficient balance"
        );

        _balances[studentNo] -= amount;
        totalBurned += amount;

        emit Burned(studentNo, amount, exchangeId, block.timestamp);
    }



    /// @notice 查询指定学号的积分余额
    /// @param studentNo 学生学号
    /// @return 积分余额
    function balanceOf(string memory studentNo)
        external
        view
        returns (uint256)
    {
        return _balances[studentNo];
    }

    /// @notice 批量查询多个学号的积分余额
    /// @dev 供后端对账定时任务调用
    /// @param studentNos 学号数组
    /// @return 对应的余额数组，长度与输入一致
    function batchBalanceOf(string[] memory studentNos)
        external
        view
        returns (uint256[] memory)
    {
        uint256[] memory results = new uint256[](studentNos.length);
        for (uint256 i = 0; i < studentNos.length; i++) {
            results[i] = _balances[studentNos[i]];
        }
        return results;
    }



    /// @notice 转移管理员权限
    /// @dev 仅当前管理员可调用
    /// @param newAdmin 新管理员地址
    function changeAdmin(address newAdmin)
        external
        onlyAdmin
    {
        require(newAdmin != address(0), "CarbonPoints: zero admin address");
        require(newAdmin != admin, "CarbonPoints: same admin");
        address oldAdmin = admin;
        admin = newAdmin;
        emit AdminChanged(oldAdmin, newAdmin);
    }



    modifier onlyAdmin() {
        require(msg.sender == admin, "CarbonPoints: caller is not admin");
        _;
    }
}
