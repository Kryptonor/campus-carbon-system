# FISCO BCOS 账户密钥目录

请将 admin 账户的 PEM 密钥文件放入此目录。

| 文件 | 说明 |
|------|------|
| `0x...696.pem` | 管理员账户 ECDSA 私钥（PEM 格式） |
| `0x...696.pem.pub` | 管理员账户 ECDSA 公钥 |

当前使用的管理员地址：`0x2783c49cb3f8fc5bef8c544a00139fdba6c6f696`

获取方式：
```bash
# 从 FISCO BCOS 节点的 account 目录复制
cp ~/fisco/nodes/127.0.0.1/account/0x2783c49cb3f8fc5bef8c544a00139fdba6c6f696.pem* backend/src/main/resources/account/

# 或从云端节点获取（参考 DEPLOY.md）
scp root@<云主机IP>:~/fisco/sdk-config/account/* backend/src/main/resources/account/
```

> ⚠️ `.pem` 文件是区块链管理员私钥，拥有 mint/burn 权限，**绝不提交到 Git 仓库**！
