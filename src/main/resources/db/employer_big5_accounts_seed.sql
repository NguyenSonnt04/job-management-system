-- Seed 5 employer accounts for major companies
-- Password for all accounts: 123456
-- Password is stored as BCrypt hash for Spring Security login
--
-- How to run in phpMyAdmin:
--   1. Open database `qltd_db`
--   2. Go to tab `Import`
--   3. Choose this file and execute

SET NAMES utf8mb4;

INSERT INTO roles (name, description)
SELECT 'ROLE_EMPLOYER', 'Nhà tuyển dụng'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = 'ROLE_EMPLOYER'
);

SET @role_employer_id := (
    SELECT id
    FROM roles
    WHERE name = 'ROLE_EMPLOYER'
    LIMIT 1
);

SET @bcrypt_123456 := '$2b$12$Xt5rtISj1ngiC6UzKeFzweIFcPuW0f0W.TdSb5LpnCTtr.3VWa3DC';

INSERT INTO users (
    email,
    password,
    full_name,
    phone,
    contact_email,
    occupation,
    enabled,
    provider,
    provider_id,
    created_at,
    updated_at,
    role_id
)
SELECT
    'talent@fptsoftware.vn',
    @bcrypt_123456,
    'Nguyen Van Hung',
    '0901113001',
    'talent@fptsoftware.vn',
    'Employer',
    1,
    'LOCAL',
    NULL,
    NOW(),
    NOW(),
    @role_employer_id
FROM DUAL
WHERE @role_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'talent@fptsoftware.vn');

SET @user_fpt := (
    SELECT id
    FROM users
    WHERE email = 'talent@fptsoftware.vn'
    LIMIT 1
);

INSERT INTO employers (
    company_name,
    business_type,
    employee_count,
    country,
    province,
    address,
    description,
    contact_name,
    contact_phone,
    tax_code,
    logo_url,
    website,
    created_at,
    updated_at,
    user_id
)
SELECT
    'FPT Software',
    'Công nghệ thông tin',
    '5000+',
    'Việt Nam',
    'TP. Hồ Chí Minh',
    'Lô T2, Đường D1, Khu Công nghệ cao, TP. Thủ Đức, TP. Hồ Chí Minh',
    'FPT Software là doanh nghiệp công nghệ lớn tại Việt Nam, thường xuyên tuyển dụng các vị trí kỹ sư phần mềm, kiểm thử, BA và quản lý dự án.',
    'Nguyen Van Hung',
    '0901113001',
    '0101248141',
    'https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg',
    'https://fptsoftware.com',
    NOW(),
    NOW(),
    @user_fpt
FROM DUAL
WHERE @user_fpt IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM employers WHERE user_id = @user_fpt);

INSERT INTO users (
    email, password, full_name, phone, contact_email, occupation, enabled, provider, provider_id, created_at, updated_at, role_id
)
SELECT
    'careers@vng.com.vn',
    @bcrypt_123456,
    'Tran Thi Mai',
    '0901113002',
    'careers@vng.com.vn',
    'Employer',
    1,
    'LOCAL',
    NULL,
    NOW(),
    NOW(),
    @role_employer_id
FROM DUAL
WHERE @role_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'careers@vng.com.vn');

SET @user_vng := (
    SELECT id
    FROM users
    WHERE email = 'careers@vng.com.vn'
    LIMIT 1
);

INSERT INTO employers (
    company_name, business_type, employee_count, country, province, address, description,
    contact_name, contact_phone, tax_code, logo_url, website, created_at, updated_at, user_id
)
SELECT
    'VNG Corporation',
    'Công nghệ thông tin',
    '3000-5000',
    'Việt Nam',
    'TP. Hồ Chí Minh',
    '182 Lê Đại Hành, Phường 15, Quận 11, TP. Hồ Chí Minh',
    'VNG là doanh nghiệp công nghệ lớn với các sản phẩm nổi bật như Zalo, Zing và các nền tảng số phục vụ hàng triệu người dùng.',
    'Tran Thi Mai',
    '0901113002',
    '0301476977',
    'https://brand.zalo.me/wp-content/uploads/2021/07/VNG-logo.png',
    'https://vng.com.vn',
    NOW(),
    NOW(),
    @user_vng
FROM DUAL
WHERE @user_vng IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM employers WHERE user_id = @user_vng);

INSERT INTO users (
    email, password, full_name, phone, contact_email, occupation, enabled, provider, provider_id, created_at, updated_at, role_id
)
SELECT
    'recruitment@tiki.vn',
    @bcrypt_123456,
    'Le Minh Tuan',
    '0901113003',
    'recruitment@tiki.vn',
    'Employer',
    1,
    'LOCAL',
    NULL,
    NOW(),
    NOW(),
    @role_employer_id
FROM DUAL
WHERE @role_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'recruitment@tiki.vn');

SET @user_tiki := (
    SELECT id
    FROM users
    WHERE email = 'recruitment@tiki.vn'
    LIMIT 1
);

INSERT INTO employers (
    company_name, business_type, employee_count, country, province, address, description,
    contact_name, contact_phone, tax_code, logo_url, website, created_at, updated_at, user_id
)
SELECT
    'Tiki Corporation',
    'Thương mại điện tử',
    '1000-3000',
    'Việt Nam',
    'TP. Hồ Chí Minh',
    '52 Út Tịch, Phường 4, Quận Tân Bình, TP. Hồ Chí Minh',
    'Tiki là nền tảng thương mại điện tử lớn tại Việt Nam, thường tuyển dụng các vị trí công nghệ, vận hành, marketing và chăm sóc khách hàng.',
    'Le Minh Tuan',
    '0901113003',
    '0312456789',
    'https://salt.tikicdn.com/ts/upload/e4/49/6c/tiki-logo.png',
    'https://tiki.vn',
    NOW(),
    NOW(),
    @user_tiki
FROM DUAL
WHERE @user_tiki IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM employers WHERE user_id = @user_tiki);

INSERT INTO users (
    email, password, full_name, phone, contact_email, occupation, enabled, provider, provider_id, created_at, updated_at, role_id
)
SELECT
    'hiring@vingroup.vn',
    @bcrypt_123456,
    'Pham Hoang Nam',
    '0901113004',
    'hiring@vingroup.vn',
    'Employer',
    1,
    'LOCAL',
    NULL,
    NOW(),
    NOW(),
    @role_employer_id
FROM DUAL
WHERE @role_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'hiring@vingroup.vn');

SET @user_vingroup := (
    SELECT id
    FROM users
    WHERE email = 'hiring@vingroup.vn'
    LIMIT 1
);

INSERT INTO employers (
    company_name, business_type, employee_count, country, province, address, description,
    contact_name, contact_phone, tax_code, logo_url, website, created_at, updated_at, user_id
)
SELECT
    'Vingroup',
    'Đa ngành',
    '10000+',
    'Việt Nam',
    'Hà Nội',
    'Số 7, Đường Bằng Lăng 1, Khu đô thị Vinhomes Riverside, Hà Nội',
    'Vingroup là tập đoàn lớn hoạt động trong nhiều lĩnh vực như bất động sản, công nghệ, giáo dục, y tế và thương mại dịch vụ.',
    'Pham Hoang Nam',
    '0901113004',
    '0101245886',
    'https://vingroup.net/themes/flavor/images/logo.png',
    'https://vingroup.net',
    NOW(),
    NOW(),
    @user_vingroup
FROM DUAL
WHERE @user_vingroup IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM employers WHERE user_id = @user_vingroup);

INSERT INTO users (
    email, password, full_name, phone, contact_email, occupation, enabled, provider, provider_id, created_at, updated_at, role_id
)
SELECT
    'talent@momo.vn',
    @bcrypt_123456,
    'Vo Thi Lan',
    '0901113005',
    'talent@momo.vn',
    'Employer',
    1,
    'LOCAL',
    NULL,
    NOW(),
    NOW(),
    @role_employer_id
FROM DUAL
WHERE @role_employer_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'talent@momo.vn');

SET @user_momo := (
    SELECT id
    FROM users
    WHERE email = 'talent@momo.vn'
    LIMIT 1
);

INSERT INTO employers (
    company_name, business_type, employee_count, country, province, address, description,
    contact_name, contact_phone, tax_code, logo_url, website, created_at, updated_at, user_id
)
SELECT
    'MoMo',
    'Fintech',
    '1000-3000',
    'Việt Nam',
    'TP. Hồ Chí Minh',
    'Tòa nhà Phú Nhuận Plaza, 82 Trần Huy Liệu, Quận Phú Nhuận, TP. Hồ Chí Minh',
    'MoMo là nền tảng ví điện tử và thanh toán số lớn tại Việt Nam, tuyển dụng mạnh các vị trí công nghệ, dữ liệu và sản phẩm.',
    'Vo Thi Lan',
    '0901113005',
    '0314567890',
    'https://homepage.momocdn.net/img/momo-logo.png',
    'https://momo.vn',
    NOW(),
    NOW(),
    @user_momo
FROM DUAL
WHERE @user_momo IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM employers WHERE user_id = @user_momo);

SELECT email, full_name, enabled
FROM users
WHERE email IN (
    'talent@fptsoftware.vn',
    'careers@vng.com.vn',
    'recruitment@tiki.vn',
    'hiring@vingroup.vn',
    'talent@momo.vn'
)
ORDER BY email;
