# 隐私政策部署说明

## 部署到GitHub Pages

### 步骤1：创建GitHub仓库
1. 在GitHub上创建一个新的公开仓库，命名为 `privacy-policy`
2. 仓库描述：音视通转应用隐私政策

### 步骤2：上传隐私政策文件
1. 将 `privacy_policy.html` 文件重命名为 `index.html`
2. 上传到仓库根目录

### 步骤3：启用GitHub Pages
1. 进入仓库设置 (Settings)
2. 找到 "Pages" 选项
3. 在 "Source" 中选择 "Deploy from a branch"
4. 选择 "main" 分支和 "/ (root)" 文件夹
5. 点击 "Save"

### 步骤4：获取访问链接
部署完成后，您的隐私政策将通过以下链接访问：
```
https://[你的GitHub用户名].github.io/privacy-policy/
```

## 应用市场后台填写

### 华为应用市场
1. 登录华为开发者联盟
2. 进入应用管理
3. 选择"音视通转"应用
4. 在"应用信息"中找到"隐私政策"
5. 填写隐私政策链接

### 小米应用商店
1. 登录小米开发者平台
2. 进入应用管理
3. 选择"音视通转"应用
4. 在"应用信息"中找到"隐私政策"
5. 填写隐私政策链接

### 应用宝
1. 登录腾讯开放平台
2. 进入应用管理
3. 选择"音视通转"应用
4. 在"应用信息"中找到"隐私政策"
5. 填写隐私政策链接

### 其他应用市场
- OPPO软件商店
- vivo应用商店
- 魅族应用商店
- 360手机助手
- 百度手机助手

## 注意事项

1. **链接格式**：确保链接格式为 `https://` 开头
2. **内容合规**：隐私政策内容需符合各应用市场要求
3. **更新维护**：政策更新时需同步更新网页和通知用户
4. **备份**：建议在多个平台备份隐私政策文件

## 快速部署命令

如果您使用Git命令行：

```bash
# 克隆仓库（替换为您的仓库地址）
git clone https://github.com/[用户名]/privacy-policy.git
cd privacy-policy

# 复制隐私政策文件
cp ../privacy_policy.html index.html

# 提交并推送
git add index.html
git commit -m "添加音视通转隐私政策"
git push origin main
```

部署完成后，等待几分钟即可通过GitHub Pages链接访问隐私政策。 