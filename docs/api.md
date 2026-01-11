# OpenRank 接口文档（简版）

## 认证
- `POST /api/auth/register`：请求体 `{ username, password, email? }`，成功返回 `{ message, token, username }`。
- `POST /api/auth/login`：请求体 `{ username, password }`，成功返回 `{ message, token, username }`。
> 认证成功后，将 `token` 作为 `X-Token` 请求头传给需要登录态的接口。

## 收藏
- `GET /api/favorites`：Header `X-Token`，返回当前用户收藏列表 `[{ id, userId, repo }]`。
- `POST /api/favorites/toggle`：Header `X-Token`，Body `{ repo }`，切换收藏状态，返回 `{ favorited: boolean, message }`。
- `POST /api/favorites`：Header `X-Token`，Body `{ repo }`，添加收藏。
- `DELETE /api/favorites`：Header `X-Token`，Body `{ repo }`，取消收藏。

## 项目/排行榜
- `GET /api/projects`：返回预置/缓存的项目清单。
- `GET /api/projects/discovery?limit=12&periodType=MONTH&order=openrank`：从数据库排行榜读取最新期的项目。
- `GET /api/projects/all?limit=400`：合并月榜、周榜及预置列表的全量仓库。
- `GET /api/projects/metrics?repo={owner/repo}`：返回指定仓库的 OpenRank/Stars 序列（最长 12 期）。

## 仓库信息（新增）
- `GET /api/repo/{owner}/{repo}`  
  - 路径参数：`owner` 仓库所有者，`repo` 仓库名。  
  - 返回：`Repo` 对象（`{ id, owner, repo, fullName, displayName, description, tags, status, priority, openrank, stars, period, periodType, updatedAt }`）。  
  - 404：未找到时返回 `{ "message": "未找到仓库" }`。

- `GET /api/repo/full/{fullName}`  
  - 路径参数：`fullName`，形如 `owner/repo`。  
  - 返回：同上。  
  - 404：未找到时返回 `{ "message": "未找到仓库" }`。

## 通用说明
- 所有返回均为 JSON。
- 需要登录的接口通过请求头 `X-Token` 传递会话 Token。***
