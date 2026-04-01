-- ============================================================
-- SEED: interview_roles
-- Danh sách vị trí/ngành nghề hỗ trợ mock interview
-- Chạy sau khi tạo bảng (ddl-auto=update sẽ tự tạo bảng)
-- ============================================================

DELETE FROM interview_roles;

INSERT INTO interview_roles (role_key, role_name, category, category_key, icon_class, display_order, active) VALUES

-- ── Công nghệ thông tin ──────────────────────────────────
('frontend_developer',   'Frontend Developer',      'Công nghệ thông tin', 'tech', 'fa-solid fa-laptop-code',    1,  1),
('backend_developer',    'Backend Developer',       'Công nghệ thông tin', 'tech', 'fa-solid fa-server',          2,  1),
('fullstack_developer',  'Fullstack Developer',     'Công nghệ thông tin', 'tech', 'fa-solid fa-layer-group',     3,  1),
('mobile_developer',     'Mobile Developer',        'Công nghệ thông tin', 'tech', 'fa-solid fa-mobile-screen',   4,  1),
('data_analyst',         'Data Analyst',            'Công nghệ thông tin', 'tech', 'fa-solid fa-chart-bar',       5,  1),
('data_scientist',       'Data Scientist',          'Công nghệ thông tin', 'tech', 'fa-solid fa-brain',           6,  1),
('data_engineer',        'Data Engineer',           'Công nghệ thông tin', 'tech', 'fa-solid fa-database',        7,  1),
('devops_engineer',      'DevOps Engineer',         'Công nghệ thông tin', 'tech', 'fa-solid fa-infinity',        8,  1),
('cloud_engineer',       'Cloud Engineer',          'Công nghệ thông tin', 'tech', 'fa-solid fa-cloud',           9,  1),
('qa_engineer',          'QA/Test Engineer',        'Công nghệ thông tin', 'tech', 'fa-solid fa-bug',            10,  1),
('security_engineer',    'Security Engineer',       'Công nghệ thông tin', 'tech', 'fa-solid fa-shield-halved',  11,  1),
('ai_ml_engineer',       'AI/ML Engineer',          'Công nghệ thông tin', 'tech', 'fa-solid fa-robot',          12,  1),
('product_manager',      'Product Manager (Tech)',  'Công nghệ thông tin', 'tech', 'fa-solid fa-diagram-project',13,  1),
('ui_ux_designer',       'UI/UX Designer',          'Công nghệ thông tin', 'tech', 'fa-solid fa-pen-nib',        14,  1),
('business_analyst_it',  'Business Analyst (IT)',   'Công nghệ thông tin', 'tech', 'fa-solid fa-magnifying-glass-chart', 15, 1),
('system_admin',         'System Administrator',    'Công nghệ thông tin', 'tech', 'fa-solid fa-terminal',       16,  1),
('blockchain_developer', 'Blockchain Developer',    'Công nghệ thông tin', 'tech', 'fa-solid fa-cube',           17,  1),

-- ── Kinh doanh & Marketing ──────────────────────────────
('business_development', 'Business Development',    'Kinh doanh & Marketing', 'business', 'fa-solid fa-handshake',        1, 1),
('sales_executive',      'Sales Executive',         'Kinh doanh & Marketing', 'business', 'fa-solid fa-bullhorn',         2, 1),
('key_account_manager',  'Key Account Manager',     'Kinh doanh & Marketing', 'business', 'fa-solid fa-user-tie',         3, 1),
('digital_marketing',    'Digital Marketing',       'Kinh doanh & Marketing', 'business', 'fa-solid fa-hashtag',          4, 1),
('content_marketing',    'Content Marketing',       'Kinh doanh & Marketing', 'business', 'fa-solid fa-pen-to-square',    5, 1),
('seo_specialist',       'SEO/SEM Specialist',      'Kinh doanh & Marketing', 'business', 'fa-solid fa-magnifying-glass', 6, 1),
('brand_manager',        'Brand Manager',           'Kinh doanh & Marketing', 'business', 'fa-solid fa-star',             7, 1),
('ecommerce_specialist', 'E-commerce Specialist',   'Kinh doanh & Marketing', 'business', 'fa-solid fa-cart-shopping',    8, 1),
('project_manager',      'Project Manager',         'Kinh doanh & Marketing', 'business', 'fa-solid fa-list-check',       9, 1),

-- ── Tài chính & Kế toán ─────────────────────────────────
('accountant',           'Kế toán viên',            'Tài chính & Kế toán', 'finance', 'fa-solid fa-calculator',    1, 1),
('chief_accountant',     'Kế toán trưởng',          'Tài chính & Kế toán', 'finance', 'fa-solid fa-file-invoice-dollar', 2, 1),
('financial_analyst',    'Financial Analyst',       'Tài chính & Kế toán', 'finance', 'fa-solid fa-chart-line',    3, 1),
('auditor',              'Kiểm toán viên',          'Tài chính & Kế toán', 'finance', 'fa-solid fa-scale-balanced',4, 1),
('tax_specialist',       'Chuyên viên Thuế',        'Tài chính & Kế toán', 'finance', 'fa-solid fa-receipt',       5, 1),
('investment_analyst',   'Investment Analyst',      'Tài chính & Kế toán', 'finance', 'fa-solid fa-coins',         6, 1),
('risk_manager',         'Risk Manager',            'Tài chính & Kế toán', 'finance', 'fa-solid fa-triangle-exclamation', 7, 1),

-- ── Nhân sự (HR) ────────────────────────────────────────
('hr_generalist',        'HR Generalist',           'Nhân sự', 'hr', 'fa-solid fa-people-group',    1, 1),
('hr_recruiter',         'Recruiter / Headhunter',  'Nhân sự', 'hr', 'fa-solid fa-user-plus',       2, 1),
('hr_training',          'Training & Development',  'Nhân sự', 'hr', 'fa-solid fa-graduation-cap',  3, 1),
('hr_compensation',      'C&B Specialist',          'Nhân sự', 'hr', 'fa-solid fa-money-bill-wave', 4, 1),
('hr_manager',           'HR Manager',              'Nhân sự', 'hr', 'fa-solid fa-user-shield',     5, 1),

-- ── Thiết kế & Sáng tạo ─────────────────────────────────
('graphic_designer',     'Graphic Designer',        'Thiết kế & Sáng tạo', 'design', 'fa-solid fa-palette',     1, 1),
('motion_designer',      'Motion Designer',         'Thiết kế & Sáng tạo', 'design', 'fa-solid fa-film',        2, 1),
('product_designer',     'Product Designer',        'Thiết kế & Sáng tạo', 'design', 'fa-solid fa-compass-drafting', 3, 1),
('3d_designer',          '3D Designer',             'Thiết kế & Sáng tạo', 'design', 'fa-solid fa-cube',        4, 1),
('copywriter',           'Copywriter',              'Thiết kế & Sáng tạo', 'design', 'fa-solid fa-feather-pointed', 5, 1),
('photographer',         'Photographer / Videographer', 'Thiết kế & Sáng tạo', 'design', 'fa-solid fa-camera', 6, 1),

-- ── Vận hành & Chuỗi cung ứng ───────────────────────────
('operations_manager',   'Operations Manager',      'Vận hành & Chuỗi cung ứng', 'ops', 'fa-solid fa-gears',            1, 1),
('supply_chain',         'Supply Chain Specialist', 'Vận hành & Chuỗi cung ứng', 'ops', 'fa-solid fa-truck',            2, 1),
('logistics',            'Logistics Coordinator',   'Vận hành & Chuỗi cung ứng', 'ops', 'fa-solid fa-boxes-stacked',    3, 1),
('procurement',          'Procurement Specialist',  'Vận hành & Chuỗi cung ứng', 'ops', 'fa-solid fa-cart-flatbed',     4, 1),
('quality_control',      'Quality Control',         'Vận hành & Chuỗi cung ứng', 'ops', 'fa-solid fa-clipboard-check',  5, 1),
('warehouse',            'Warehouse Supervisor',    'Vận hành & Chuỗi cung ứng', 'ops', 'fa-solid fa-warehouse',        6, 1),

-- ── Ngành khác ───────────────────────────────────────────
('customer_service',     'Customer Service',        'Ngành khác', 'other', 'fa-solid fa-headset',         1, 1),
('legal_counsel',        'Legal Counsel',           'Ngành khác', 'other', 'fa-solid fa-gavel',           2, 1),
('pr_specialist',        'PR Specialist',           'Ngành khác', 'other', 'fa-solid fa-newspaper',       3, 1),
('teacher_trainer',      'Giáo viên / Đào tạo',    'Ngành khác', 'other', 'fa-solid fa-chalkboard-user', 4, 1),
('healthcare',           'Y tế / Điều dưỡng',      'Ngành khác', 'other', 'fa-solid fa-hospital-user',   5, 1),
('real_estate',          'Bất động sản',            'Ngành khác', 'other', 'fa-solid fa-building',        6, 1),
('administrative',       'Hành chính / Văn phòng',  'Ngành khác', 'other', 'fa-solid fa-folder-open',    7, 1),
('banking',              'Ngân hàng / Tín dụng',    'Ngành khác', 'other', 'fa-solid fa-landmark',        8, 1);
