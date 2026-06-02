-- 种子数据
MERGE INTO templates (id, name, type, is_premium, is_active, config, tags, description, sort_order) VALUES
('tpl_001', '简洁表格款', 'simple', 0, 1, '{"layout":"table","columns":["image","name","price","note"],"maxItemsPerPage":25}', '["大量出物","信息清晰"]', '适合一次性整理大量出物，信息密度高', 1);

MERGE INTO templates (id, name, type, is_premium, is_active, config, tags, description, sort_order) VALUES
('tpl_002', '卡片展示款', 'card', 0, 1, '{"layout":"card","columns":2,"maxItemsPerPage":12}', '["小红书","好看"]', '每件商品一张小卡片，适合小红书/朋友圈分享', 2);

MERGE INTO templates (id, name, type, is_premium, is_active, config, tags, description, sort_order) VALUES
('tpl_003', '图片墙款', 'wall', 0, 1, '{"layout":"grid","columns":3,"maxItemsPerPage":30}', '["图片多","视觉冲击"]', '强调图片展示，适合小卡/吧唧/立牌', 3);
