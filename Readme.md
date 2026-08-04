#  Concert & Booking System API Documentation

Chào mừng đến với hệ thống Đặt vé và Quản lý Concert. Tài liệu này cung cấp toàn bộ hướng dẫn từ quy tắc code, cách thiết lập môi trường, đến tài liệu API.

---

##  1. Cách thiết lập & Chạy mã nguồn ở môi trường Local

Hệ thống được phát triển dựa trên **Spring Boot 3.x** và **Java 17**.

### Yêu cầu cài đặt (Prerequisites)
- **Java JDK 17** trở lên.
- **Maven** (hoặc dùng wrapper `mvnw` đi kèm source code).
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code.

### Các bước khởi chạy
1. **Clone/Tải mã nguồn** về máy tính.
2. Mở Terminal / Command Prompt tại thư mục gốc của dự án.
3. Chạy lệnh cài đặt thư viện và build dự án:
   ```bash
   mvn clean install -DskipTests
   ```
4. Khởi động Spring Boot Application:
   ```bash
   mvn spring-boot:run
   ```
   *Hoặc bạn có thể mở class có chứa `@SpringBootApplication` trong IDE và nhấn nút **Run**.*
5. **Cơ sở dữ liệu**: Dự án đã được cấu hình tự động chạy Data Seeding từ file `seed_data.sql`. Hệ thống sẽ tự động khởi tạo dữ liệu mẫu mỗi khi chạy.
6. Hệ thống sẽ lắng nghe ở cổng mặc định: `http://localhost:8080`.

---

##  2. Hướng dẫn & Quy ước viết code (Coding Guideline & Convention)

Để giữ cho dự án luôn đồng nhất và dễ bảo trì, mọi thành viên cần tuân thủ các quy tắc sau:

### Quy ước thiết kế API (RESTful Convention)
- **Base URL**: Tất cả API đều phải có tiền tố `/api/v1/`.
- **Naming**: Sử dụng **Danh từ số nhiều** cho các resource (Ví dụ: `/api/v1/users`, `/api/v1/bookings`).
- **HTTP Methods**:
  - `GET`: Để lấy dữ liệu.
  - `POST`: Để tạo mới dữ liệu.
  - `PUT`: Để cập nhật toàn bộ dữ liệu.
  - `PATCH`: Để cập nhật một phần dữ liệu (chỉ định).
  - `DELETE`: Để xóa dữ liệu.
- **Response Format**: Trả về dữ liệu bọc trong `BaseResponse` chuẩn của dự án:
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": { ... }
  }
  ```

### Luồng viết một API mới (Cách thêm tính năng)
1. **DTOs**: Khai báo các class Request/Response trong thư mục `dto/request` và `dto/response`.
2. **Controller**: Viết method trong Controller, khai báo `@Operation` và `@Tag` của Swagger. Gọi Service.
3. **Service Interface**: Định nghĩa method trong thư mục `service`.
4. **Service Impl**: Triển khai logic nghiệp vụ tại `service/impl`. Sử dụng `@Transactional` cho các thao tác thay đổi DB.
5. **Repository**: Định nghĩa các Query tương tác với database.

### Quy ước viết Unit Test
- Framework sử dụng: **JUnit 5** và **Mockito**.
- Vị trí: `src/test/java/com/huynhliem/...`
- Cách chạy test toàn hệ thống:
  ```bash
  mvn test
  ```
- **Nguyên tắc**: Test Service layer phải Mock toàn bộ Repository. Không gọi trực tiếp DB thật khi test.

---

##  3. Tài liệu API (Swagger UI)

Hệ thống đã được tích hợp sẵn Swagger OpenAPI 3.0 để tự động sinh tài liệu và cung cấp giao diện Test trực quan.

- **Đường dẫn truy cập Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Cách sử dụng**:
  1. Mở trình duyệt và truy cập link trên (sau khi đã start server).
  2. Ở góc trên bên phải, bạn có thể chọn **Select a definition** (như Booking, Concert, User...) để lọc API theo chức năng.
  3. Để gọi các API cần quyền (như Admin/User), bạn cần lấy Token từ API `POST /api/v1/auth/login`.
  4. Bấm vào nút **Authorize (ổ khóa)** và điền chuỗi Token vào, sau đó bấm Save.

---

##  4. Bộ sưu tập kiểm thử API (Postman Collection)

Để thuận tiện cho việc test thủ công hoặc automation test, tôi đã tạo sẵn một file Postman Collection chứa toàn bộ các API của dự án.

- **File đính kèm**: `BookingSystem_Postman_Collection.json` (nằm ở thư mục gốc của dự án).
- **Hướng dẫn import**:
  1. Mở phần mềm Postman.
  2. Nhấn nút **Import** ở góc trái trên cùng.
  3. Kéo thả file `BookingSystem_Postman_Collection.json` vào.
  4. Mở từng thư mục và gọi API (Token có thể thiết lập ở thư mục gốc của Collection).
