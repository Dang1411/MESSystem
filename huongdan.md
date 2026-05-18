# Hướng dẫn cài đặt và chạy MES System

## Yêu cầu hệ thống

| Thành phần | Phiên bản yêu cầu |
|---|---|
| Java JDK | 17 (khuyến nghị Eclipse Adoptium / Temurin) |
| Apache Maven | 3.6+ |
| Node.js | 16+ |
| npm | 8+ |
| SQL Server | 2019 / 2022 / 2025 |

---

## 1. Cài đặt cơ sở dữ liệu

### 1.1 Tạo database

Mở SQL Server Management Studio (SSMS) hoặc `sqlcmd`, kết nối với SQL Server bằng tài khoản `sa`, sau đó chạy:

```sql
CREATE DATABASE MES_DB
    COLLATE Vietnamese_CI_AS;
GO
```

> **Lưu ý:** Collation `Vietnamese_CI_AS` đảm bảo lưu trữ và so sánh tiếng Việt đúng.

### 1.2 Cấu hình tài khoản

Mặc định project dùng:

| Thông số | Giá trị |
|---|---|
| Server | `localhost,1433` |
| Database | `MES_DB` |
| Username | `sa` |
| Password | `MesApp@2024` |

Nếu muốn thay đổi, chỉnh lại trong file `backend/src/main/resources/application.properties`.

---

## 2. Cài đặt và chạy Backend (Spring Boot)

### 2.1 Yêu cầu

- Đã cài **Java 17** và thiết lập biến môi trường `JAVA_HOME`.
- Đã cài **Apache Maven** và thêm vào `PATH`.

Kiểm tra:

```powershell
java -version
mvn -version
```

### 2.2 Chạy backend

Mở terminal, vào thư mục `backend`:

```powershell
cd backend
mvn spring-boot:run
```

Lần đầu chạy Maven sẽ tự động tải dependencies (có thể mất vài phút). Khi thấy log:

```
Started MesBackendApplication in X.XXX seconds
```

là backend đã sẵn sàng tại `http://localhost:8080`.

### 2.3 Dữ liệu mẫu

Lần đầu khởi động, hệ thống tự động tạo dữ liệu mẫu gồm:

- **3 vai trò:** SUPERVISOR, OPERATOR, QC
- **7 tài khoản demo** (xem bảng tài khoản bên dưới)
- **5 sản phẩm**, **5 công đoạn**, **6 lệnh sản xuất**, **51 serial**, **125 lịch sử sản xuất**

---

## 3. Cài đặt và chạy Frontend (Vue.js)

### 3.1 Cài dependencies

Mở terminal mới, vào thư mục `frontend`:

```powershell
cd frontend
npm install
```

### 3.2 Chạy dev server

```powershell
npm run serve
```

Khi thấy:

```
App running at:
  - Local:   http://localhost:3000/
```

Mở trình duyệt tại `http://localhost:3000`.

---

## 4. Tài khoản đăng nhập demo

| Username | Password | Vai trò | Mô tả |
|---|---|---|---|
| `supervisor` | `123456` | SUPERVISOR | Quản lý cấp cao |
| `supervisor2` | `123456` | SUPERVISOR | Quản lý ca 2 |
| `operator` | `123456` | OPERATOR | Vận hành viên |
| `operator2` | `123456` | OPERATOR | Vận hành viên 2 |
| `operator3` | `123456` | OPERATOR | Vận hành viên 3 |
| `qc` | `123456` | QC | Kiểm tra chất lượng |
| `qc2` | `123456` | QC | Kiểm tra chất lượng 2 |

---

## 5. Cấu trúc thư mục

```
deadlineDn1/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/mes/
│   │   ├── config/             # Cấu hình (Security, JWT, DataInitializer)
│   │   ├── controller/         # REST Controllers
│   │   ├── entity/             # JPA Entities
│   │   ├── repository/         # Spring Data Repositories
│   │   └── service/            # Business Logic
│   └── src/main/resources/
│       └── application.properties
├── frontend/                   # Vue.js SPA
│   ├── src/
│   │   ├── api/                # Axios API calls
│   │   ├── components/         # Vue Components
│   │   ├── router/             # Vue Router
│   │   ├── store/              # Vuex Store
│   │   └── views/              # Trang chính
│   └── vue.config.js           # Proxy /api → localhost:8080
├── database/
│   └── schema.sql              # Script tạo schema thủ công (tuỳ chọn)
└── huongdan.md                 # File này
```

---

## 6. API Endpoints chính

| Endpoint | Mô tả |
|---|---|
| `POST /api/auth/login` | Đăng nhập, nhận JWT token |
| `GET /api/products` | Danh sách sản phẩm |
| `GET /api/process-steps` | Danh sách công đoạn |
| `GET /api/production-orders` | Danh sách lệnh sản xuất |
| `GET /api/production-orders/{id}/serials` | Serial theo lệnh |
| `POST /api/production/scan` | Quét serial sản xuất |
| `GET /api/traceability/{serialNumber}` | Truy xuất nguồn gốc |
| `GET /api/defects` | Danh sách lỗi |
| `GET /api/defect-logs` | Nhật ký lỗi |
| `GET /api/dashboard/stats` | Thống kê tổng quan |

Tất cả endpoint (trừ `/api/auth/login`) yêu cầu header:

```
Authorization: Bearer <jwt_token>
```

---

## 7. Xử lý sự cố thường gặp

### Backend không khởi động được

**Lỗi kết nối database:**
```
Cannot open database "MES_DB"
```
→ Kiểm tra SQL Server đang chạy, database đã tạo, username/password đúng trong `application.properties`.

**Lỗi port 8080 bị chiếm:**
```
Web server failed to start. Port 8080 was already in use.
```
→ Tìm và dừng tiến trình đang dùng port 8080:
```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess
Stop-Process -Id <PID>
```

### Frontend không kết nối được API

Proxy trong `vue.config.js` chuyển `/api` → `http://localhost:8080`. Đảm bảo backend đã chạy trước khi khởi động frontend.

### Dữ liệu mẫu không được tạo

Dữ liệu mẫu chỉ tạo khi database **rỗng** (không có Role nào). Nếu cần reset:

```sql
-- Chạy trong SSMS
USE MES_DB;
-- Xóa dữ liệu theo thứ tự (foreign key)
DELETE FROM defect_logs;
DELETE FROM production_histories;
DELETE FROM product_serials;
DELETE FROM production_orders;
DELETE FROM product_process_routes;
DELETE FROM defects;
DELETE FROM products;
DELETE FROM process_steps;
DELETE FROM users;
DELETE FROM roles;
```

Sau đó khởi động lại backend.

---

## 8. Công nghệ sử dụng

**Backend:**
- Spring Boot 2.7.18
- Spring Security + JWT
- Spring Data JPA / Hibernate
- SQL Server JDBC Driver

**Frontend:**
- Vue.js 2.6.14
- Vue Router 3.x
- Vuex 3.x
- Axios
- Bootstrap 4.6.2
- Chart.js 2.x
