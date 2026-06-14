# FISCO BCOS SDK 证书目录

请将以下 3 个文件放入此目录：

| 文件 | 来源 | 说明 |
|------|------|------|
| `ca.crt` | FISCO BCOS 节点的 `sdk/` 目录 | CA 根证书 |
| `sdk.crt` | FISCO BCOS 节点的 `sdk/` 目录 | SDK 客户端证书 |
| `sdk.key` | FISCO BCOS 节点的 `sdk/` 目录 | SDK 客户端私钥 |

获取方式：
```bash
# 从 FISCO BCOS 节点复制（以本地 WSL2 为例）
cp ~/fisco/nodes/127.0.0.1/sdk/* backend/src/main/resources/conf/

# 或从云端节点获取（参考 DEPLOY.md）
scp root@<云主机IP>:~/fisco/sdk-config/conf/* backend/src/main/resources/conf/
```

> ⚠️ 这些文件包含私钥，**绝不提交到 Git 仓库**！已在 `.gitignore` 中排除。
