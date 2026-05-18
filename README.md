# MES System — Hệ thống Quản trị Sản xuất Linh kiện Điện tử

> Đồ án tốt nghiệp: Xây dựng hệ thống quản trị MES của doanh nghiệp sản xuất linh kiện điện tử

---

## Yêu cầu hệ thống

| Thành phần | Phiên bản |
|---|---|
| Java JDK | 11+ |
| Node.js | 14+ |
| Microsoft SQL Server | 2016+ (hoặc SQL Server Express) |
| Maven | 3.6+ |

---

## Cấu trúc dự án

```
deadlineDn1/
├── backend/                    # Spring Boot 2.7.18
│   ├── pom.xml
│   └── src/main/java/com/mes/
│       ├── MesApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── CorsConfig.java
│       │   └── DataInitializer.java  ← Khởi tạo dữ liệu mẫu
│       ├── entity/             # 10 JPA Entities
│       ├── repository/         # 10 Spring Data Repositories
│       ├── service/            # 10 Services
│       ├── controller/         # 10 REST Controllers
│       ├── dto/                # Request/Response DTOs
│       ├── security/           # JWT Auth
│       └── exception/          # Global Exception Handler
│
├── frontend/                   # Vue.js 2
│   ├── package.json
│   ├── vue.config.js           # Port 3000, proxy /api → 8080
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js
│       ├── store/index.js      # Vuex auth state
│       ├── api/                # Axios API services
│       ├── layouts/
│       │   └── MainLayout.vue  # Sidebar + Navbar
│       └── views/
│           ├── Login.vue
│           ├── Dashboard.vue
│           ├── Users.vue
│           ├── Products.vue
│           ├── ProcessSteps.vue
│           ├── ProductionOrders.vue
│           ├── ScanProduction.vue  ← Quét QR/barcode
│           ├── QualityCheck.vue
│           ├── Traceability.vue
│           ├── Defects.vue
│           └── Reports.vue
│
└── database/
    └── schema.sql              # SQL Server DDL (tùy chọn, JPA tự tạo)
```

---

## Cài đặt & Chạy dự án

### Bước 1: Chuẩn bị SQL Server

1. Tạo database:
```sql
CREATE DATABASE MES_DB;
```

2. Đảm bảo SQL Server Authentication được bật và tài khoản `sa` có quyền truy cập.

3. (Tùy chọn) Chạy `database/schema.sql` để tạo schema thủ công, hoặc để JPA tự tạo (`ddl-auto=update`).

---

### Bước 2: Cấu hình Backend

Mở file `backend/src/main/resources/application.properties`:

```properties
# Thay đổi password phù hợp
spring.datasource.password=YourPassword123
```

---

### Bước 3: Chạy Backend

```bash
cd backend
mvn spring-boot:run
```

Backend chạy tại: `http://localhost:8080`

Khi khởi động lần đầu, `DataInitializer` sẽ tự động tạo:
- 3 vai trò: SUPERVISOR, OPERATOR, QC
- 4 tài khoản người dùng
- 3 sản phẩm (PCB001, SEN001, MOD001)
- 5 công đoạn sản xuất
- Quy trình sản xuất cho từng sản phẩm
- 2 lệnh sản xuất mẫu (PO001, PO002)
- Lịch sử sản xuất và nhật ký lỗi demo

---

### Bước 4: Chạy Frontend

```bash
cd frontend
npm install
npm run serve
```

Frontend chạy tại: `http://localhost:3000`

---

## Tài khoản Demo

| Username | Password | Vai trò | Quyền hạn |
|---|---|---|---|
| `supervisor` | `123456` | SUPERVISOR | Toàn quyền: quản lý người dùng, sản phẩm, lệnh SX, báo cáo |
| `operator` | `123456` | OPERATOR | Quét & thực hiện sản xuất, xem lệnh SX |
| `qc` | `123456` | QC | Kiểm tra chất lượng, quản lý lỗi, truy xuất |

---

## API Endpoints chính

### Authentication
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/login` | Đăng nhập, trả về JWT token |
| GET | `/api/auth/me` | Thông tin người dùng hiện tại |

### Scan & Production
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/scan/{serialCode}` | Tra cứu thông tin serial |
| POST | `/api/scan/execute` | Thực hiện công đoạn sản xuất |

### Dashboard & Reports
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/dashboard/stats` | Thống kê tổng quan |
| GET | `/api/traceability/{serialCode}` | Truy xuất nguồn gốc |
| GET | `/api/reports/production` | Báo cáo sản xuất |
| GET | `/api/reports/quality` | Báo cáo chất lượng |
| GET | `/api/reports/serials` | Báo cáo serial |

---

## Trạng thái Serial

```
WAITING → IN_PROGRESS → OK → (tiếp tục công đoạn tiếp theo) → FINISHED
                     ↘ NG      (cần xử lý)
                     ↘ REWORK  (làm lại)
                     ↘ SCRAP   (phế phẩm, dừng hẳn)
                     ↘ HOLD    (giữ lại chờ xét)
```

---

## Công nghệ sử dụng

**Backend:**
- Spring Boot 2.7.18 + Java 11
- Spring Security 5 + JWT (JJWT 0.9.1)
- Spring Data JPA + Hibernate
- Microsoft SQL Server
- Lombok

**Frontend:**
- Vue.js 2.6
- Vue Router 3 + Vuex 3
- Axios
- Bootstrap 4
- Chart.js 2.9

---

## Build Production

```bash
# Backend
cd backend
mvn clean package -DskipTests
java -jar target/mes-backend-1.0.0.jar

# Frontend
cd frontend
npm run build
# Output: frontend/dist/ → deploy lên Nginx/Apache
```
