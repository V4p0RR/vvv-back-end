USE vvv

-- 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    description TEXT COMMENT '个人简介',
    sex ENUM('male', 'female', 'secret') DEFAULT 'secret' COMMENT '性别',
    avatar VARCHAR(512) COMMENT '头像OSS URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    status TINYINT DEFAULT 1 COMMENT '0禁用 1正常'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 博客表
CREATE TABLE blog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '博客标题',
    content LONGTEXT NOT NULL COMMENT '博客正文，支持超长Markdown或HTML',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    cover_image VARCHAR(512) COMMENT '封面图OSS URL',
    views BIGINT DEFAULT 0 COMMENT '浏览量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status TINYINT DEFAULT 1 COMMENT '0草稿 1发布',
    
    FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_author (author_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 图库表
CREATE TABLE gallery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) COMMENT '图片标题，可选',
    description TEXT COMMENT '图片描述',
    image_url VARCHAR(512) NOT NULL COMMENT '图片OSS URL',
    user_id BIGINT NOT NULL COMMENT '上传者ID',
    likes BIGINT DEFAULT 0 COMMENT '点赞数，可扩展',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE home_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    main_type VARCHAR(20) COMMENT '主展示类型: image 或 video',
    main_src VARCHAR(512) COMMENT '主展示URL',
    main_alt VARCHAR(255) COMMENT '主展示描述',
    gallery_json TEXT COMMENT '轮播图JSON数组: [{"src":"...","alt":"..."},...]',
    stamps_json TEXT COMMENT 'GIF贴纸JSON数组: [{"src":"...","alt":"..."},...]',
    pinned_blog TEXT COMMENT '置顶blog内容，支持Markdown或纯文本'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主页配置';


CREATE TABLE video (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '视频ID',
    title VARCHAR(255) NOT NULL COMMENT '视频标题',
    description TEXT COMMENT '视频描述',
    src VARCHAR(512) NOT NULL COMMENT '视频URL（从OSS上传得到）',
    thumbnail VARCHAR(512) COMMENT '缩略图URL（可选，手动上传或自动截取）',
    duration INT COMMENT '视频时长（秒）',
    tags VARCHAR(255) COMMENT '标签 用逗号分隔，如 "emo,night,rain"',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶（1=是，0=否）',
    view_count BIGINT DEFAULT 0 COMMENT '播放次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;

-- 加几个索引
CREATE INDEX idx_is_pinned ON video(is_pinned);
CREATE INDEX idx_created_at ON video(created_at DESC);
CREATE INDEX idx_tags ON video(tags);



CREATE TABLE gif (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'GIF ID',
    title VARCHAR(255) NOT NULL COMMENT 'GIF标题',
    description TEXT COMMENT 'GIF描述',
    src VARCHAR(512) NOT NULL COMMENT 'GIF URL（从OSS上传得到）',
    thumbnail VARCHAR(512) COMMENT '缩略图URL（可选，第一帧或手动上传）',
    tags VARCHAR(255) COMMENT '标签 用逗号分隔，如 "cute,fox,float,emo"',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶在主页贴纸区（1=是，0=否）',
    view_count BIGINT DEFAULT 0 COMMENT '播放/查看次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 加几个索引
CREATE INDEX idx_is_pinned ON gif(is_pinned);
CREATE INDEX idx_created_at ON gif(created_at DESC);
CREATE INDEX idx_tags ON gif(tags);
