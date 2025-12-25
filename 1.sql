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
    main_type VARCHAR(20) COMMENT '大展示类型: image/video/gif',
    main_src VARCHAR(512) COMMENT '大展示URL',
    main_title VARCHAR(255) COMMENT '标题',
    main_desc TEXT COMMENT '描述',
    main_random TINYINT(1) DEFAULT 0 COMMENT '是否随机从数据库取 1=随机 0=指定',
    gallery_json TEXT COMMENT '拼图JSON数组: [{"type":"image/video/gif","src":"...","alt":"..."},...]',
    pinned_blog_id BIGINT COMMENT '置顶Blog ID（手动指定）',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE home_config 
    ADD COLUMN main_alt VARCHAR(255) DEFAULT '' COMMENT '大展示 alt 描述' AFTER main_desc;

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



-- 音乐表（music）
CREATE TABLE music (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '音乐ID',
    title VARCHAR(255) NOT NULL COMMENT '音乐标题',
    description TEXT COMMENT '音乐描述',
    src VARCHAR(512) NOT NULL COMMENT '音乐文件URL（OSS完整地址）',
    cover_image VARCHAR(512) COMMENT '封面图URL（可选）',
    duration INT COMMENT '音乐时长（秒）',
    artist VARCHAR(100) COMMENT '歌手/艺术家',
    album VARCHAR(100) COMMENT '专辑名',
    tags VARCHAR(255) COMMENT '标签，逗号分隔，如 "lofi,chill,night"',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶（1=是，0=否）',
    view_count BIGINT DEFAULT 0 COMMENT '播放次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_is_pinned (is_pinned),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_tags (tags)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音乐资源表';

-- 照片表（photo） - 静态图片资源
CREATE TABLE photo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '照片ID',
    title VARCHAR(255) COMMENT '照片标题（可选）',
    description TEXT COMMENT '照片描述',
    src VARCHAR(512) NOT NULL COMMENT '图片URL（OSS完整地址）',
    alt VARCHAR(255) COMMENT 'alt描述（用于无障碍访问）',
    tags VARCHAR(255) COMMENT '标签，逗号分隔，如 "emo,night,portrait"',
    category VARCHAR(50) COMMENT '分类，如 "landscape","portrait","gallery"',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶（1=是，0=否）',
    likes BIGINT DEFAULT 0 COMMENT '点赞数',
    view_count BIGINT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_is_pinned (is_pinned),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_tags (tags),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片（静态图片）资源表';


-- video 表
ALTER TABLE video 
    ADD COLUMN uploader_id BIGINT DEFAULT 0 COMMENT '上传者ID' AFTER updated_at,
    ADD COLUMN uploader_username VARCHAR(50) DEFAULT 'V1rtual' COMMENT '上传者用户名' AFTER uploader_id;

-- gif 表
ALTER TABLE gif 
    ADD COLUMN uploader_id BIGINT DEFAULT 0 COMMENT '上传者ID' AFTER updated_at,
    ADD COLUMN uploader_username VARCHAR(50) DEFAULT 'V1rtual' COMMENT '上传者用户名' AFTER uploader_id;

-- music 表
ALTER TABLE music 
    ADD COLUMN uploader_id BIGINT DEFAULT 0 COMMENT '上传者ID' AFTER updated_at,
    ADD COLUMN uploader_username VARCHAR(50) DEFAULT 'V1rtual' COMMENT '上传者用户名' AFTER uploader_id;

-- photo 表
ALTER TABLE photo 
    ADD COLUMN uploader_id BIGINT DEFAULT 0 COMMENT '上传者ID' AFTER updated_at,
    ADD COLUMN uploader_username VARCHAR(50) DEFAULT 'V1rtual' COMMENT '上传者用户名' AFTER uploader_id;