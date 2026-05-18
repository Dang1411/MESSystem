-- ============================================================
-- MES SYSTEM - Manufacturing Execution System
-- Database: Microsoft SQL Server
-- Author: MES Team
-- Date: 2026
-- ============================================================

-- Tạo database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'MES_DB')
BEGIN
    CREATE DATABASE MES_DB
    COLLATE Vietnamese_CI_AS;
END
GO

USE MES_DB;
GO

-- ============================================================
-- BẢNG ROLES - Vai trò người dùng
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='roles' AND xtype='U')
BEGIN
    CREATE TABLE roles (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        name        NVARCHAR(50)  NOT NULL UNIQUE,
        description NVARCHAR(200)
    );
END
GO

-- ============================================================
-- BẢNG USERS - Người dùng hệ thống
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        employee_code   NVARCHAR(50)  NOT NULL UNIQUE,
        full_name       NVARCHAR(200) NOT NULL,
        username        NVARCHAR(100) NOT NULL UNIQUE,
        password        NVARCHAR(255) NOT NULL,
        role_id         INT           NOT NULL,
        is_active       BIT           NOT NULL DEFAULT 1,
        created_at      DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2,
        CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
    );
    CREATE INDEX idx_users_username ON users(username);
    CREATE INDEX idx_users_role     ON users(role_id);
END
GO

-- ============================================================
-- BẢNG PRODUCTS - Sản phẩm
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='products' AND xtype='U')
BEGIN
    CREATE TABLE products (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        product_code    NVARCHAR(50)  NOT NULL UNIQUE,
        product_name    NVARCHAR(200) NOT NULL,
        component_type  NVARCHAR(100),
        description     NVARCHAR(500),
        status          NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
        created_at      DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2,
        CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE','INACTIVE'))
    );
    CREATE INDEX idx_products_code ON products(product_code);
END
GO

-- ============================================================
-- BẢNG PROCESS_STEPS - Công đoạn sản xuất
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='process_steps' AND xtype='U')
BEGIN
    CREATE TABLE process_steps (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        step_code   NVARCHAR(50)  NOT NULL UNIQUE,
        step_name   NVARCHAR(200) NOT NULL,
        description NVARCHAR(500),
        is_active   BIT           NOT NULL DEFAULT 1,
        created_at  DATETIME2     NOT NULL DEFAULT GETDATE()
    );
END
GO

-- ============================================================
-- BẢNG PRODUCT_PROCESS_ROUTES - Quy trình công đoạn theo sản phẩm
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='product_process_routes' AND xtype='U')
BEGIN
    CREATE TABLE product_process_routes (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        product_id      INT NOT NULL,
        process_step_id INT NOT NULL,
        step_order      INT NOT NULL,
        is_mandatory    BIT NOT NULL DEFAULT 1,
        created_at      DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_route_product      FOREIGN KEY (product_id)      REFERENCES products(id),
        CONSTRAINT fk_route_step         FOREIGN KEY (process_step_id) REFERENCES process_steps(id),
        CONSTRAINT uq_route_product_order UNIQUE (product_id, step_order)
    );
    CREATE INDEX idx_route_product ON product_process_routes(product_id);
END
GO

-- ============================================================
-- BẢNG PRODUCTION_ORDERS - Lệnh sản xuất
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='production_orders' AND xtype='U')
BEGIN
    CREATE TABLE production_orders (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        order_code          NVARCHAR(50)  NOT NULL UNIQUE,
        product_id          INT           NOT NULL,
        planned_quantity    INT           NOT NULL,
        completed_quantity  INT           NOT NULL DEFAULT 0,
        start_date          DATE,
        end_date            DATE,
        status              NVARCHAR(20)  NOT NULL DEFAULT 'CREATED',
        created_by          INT           NOT NULL,
        created_at          DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2,
        notes               NVARCHAR(500),
        CONSTRAINT fk_order_product  FOREIGN KEY (product_id)  REFERENCES products(id),
        CONSTRAINT fk_order_creator  FOREIGN KEY (created_by)  REFERENCES users(id),
        CONSTRAINT chk_order_status  CHECK (status IN ('CREATED','IN_PROGRESS','COMPLETED','CANCELLED'))
    );
    CREATE INDEX idx_orders_status  ON production_orders(status);
    CREATE INDEX idx_orders_product ON production_orders(product_id);
END
GO

-- ============================================================
-- BẢNG PRODUCT_SERIALS - Serial sản phẩm
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='product_serials' AND xtype='U')
BEGIN
    CREATE TABLE product_serials (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        serial_code         NVARCHAR(100) NOT NULL UNIQUE,
        production_order_id INT           NOT NULL,
        product_id          INT           NOT NULL,
        current_step_id     INT,
        status              NVARCHAR(20)  NOT NULL DEFAULT 'WAITING',
        created_at          DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2,
        CONSTRAINT fk_serial_order   FOREIGN KEY (production_order_id) REFERENCES production_orders(id),
        CONSTRAINT fk_serial_product FOREIGN KEY (product_id)          REFERENCES products(id),
        CONSTRAINT fk_serial_step    FOREIGN KEY (current_step_id)     REFERENCES process_steps(id),
        CONSTRAINT chk_serial_status CHECK (status IN ('WAITING','IN_PROGRESS','OK','NG','REWORK','SCRAP','HOLD','FINISHED'))
    );
    CREATE INDEX idx_serials_order  ON product_serials(production_order_id);
    CREATE INDEX idx_serials_code   ON product_serials(serial_code);
    CREATE INDEX idx_serials_status ON product_serials(status);
END
GO

-- ============================================================
-- BẢNG PRODUCTION_HISTORIES - Lịch sử sản xuất
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='production_histories' AND xtype='U')
BEGIN
    CREATE TABLE production_histories (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        serial_id       INT           NOT NULL,
        process_step_id INT           NOT NULL,
        operator_id     INT           NOT NULL,
        start_time      DATETIME2,
        end_time        DATETIME2,
        result          NVARCHAR(20),
        notes           NVARCHAR(500),
        created_at      DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_history_serial  FOREIGN KEY (serial_id)       REFERENCES product_serials(id),
        CONSTRAINT fk_history_step    FOREIGN KEY (process_step_id) REFERENCES process_steps(id),
        CONSTRAINT fk_history_oper    FOREIGN KEY (operator_id)     REFERENCES users(id),
        CONSTRAINT chk_history_result CHECK (result IN ('OK','NG','REWORK','SCRAP','HOLD'))
    );
    CREATE INDEX idx_hist_serial ON production_histories(serial_id);
    CREATE INDEX idx_hist_step   ON production_histories(process_step_id);
    CREATE INDEX idx_hist_date   ON production_histories(created_at);
END
GO

-- ============================================================
-- BẢNG DEFECTS - Danh mục lỗi
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='defects' AND xtype='U')
BEGIN
    CREATE TABLE defects (
        id           INT IDENTITY(1,1) PRIMARY KEY,
        defect_code  NVARCHAR(50)  NOT NULL UNIQUE,
        defect_name  NVARCHAR(200) NOT NULL,
        description  NVARCHAR(500),
        is_active    BIT           NOT NULL DEFAULT 1,
        created_at   DATETIME2     NOT NULL DEFAULT GETDATE()
    );
END
GO

-- ============================================================
-- BẢNG DEFECT_LOGS - Ghi nhận lỗi
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='defect_logs' AND xtype='U')
BEGIN
    CREATE TABLE defect_logs (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        serial_id       INT           NOT NULL,
        defect_id       INT           NOT NULL,
        process_step_id INT           NOT NULL,
        reported_by     INT           NOT NULL,
        action_taken    NVARCHAR(200),
        notes           NVARCHAR(500),
        created_at      DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_dlog_serial  FOREIGN KEY (serial_id)       REFERENCES product_serials(id),
        CONSTRAINT fk_dlog_defect  FOREIGN KEY (defect_id)       REFERENCES defects(id),
        CONSTRAINT fk_dlog_step    FOREIGN KEY (process_step_id) REFERENCES process_steps(id),
        CONSTRAINT fk_dlog_user    FOREIGN KEY (reported_by)     REFERENCES users(id)
    );
    CREATE INDEX idx_dlog_serial ON defect_logs(serial_id);
    CREATE INDEX idx_dlog_step   ON defect_logs(process_step_id);
    CREATE INDEX idx_dlog_date   ON defect_logs(created_at);
END
GO

-- ============================================================
-- DỮ LIỆU MẪU - Chú ý: mật khẩu được encode BCrypt bởi DataInitializer
-- ============================================================

-- Dữ liệu sẽ được khởi tạo tự động qua DataInitializer.java khi khởi động ứng dụng
-- Tài khoản demo:
--   supervisor / 123456  (vai trò: SUPERVISOR)
--   operator   / 123456  (vai trò: OPERATOR)
--   qc         / 123456  (vai trò: QC)

PRINT 'Schema MES_DB đã được tạo thành công!';
GO
