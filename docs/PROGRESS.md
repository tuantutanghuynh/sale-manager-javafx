# PROGRESS — nhật ký session

Mới nhất ở trên cùng. Mẫu và quy tắc: `CLAUDE.md` mục 12. Soạn bằng `/wrap`.

Claude **không nhớ session trước** — file này là bộ nhớ dài hạn của dự án. Đọc nó ở đầu mỗi session.

---

## [2026-11-25] — M1 GĐ1: báo lỗi khởi động thân thiện (agy viết, Claude review)
**Milestone:** M1 (GĐ1/3 xong)

**Đã làm:**
- `App.init()` bắt mọi lỗi config/DB/migration, đóng pool, lưu lỗi; `start()` hiện dialog rồi `Platform.exit()`
- Default uncaught exception handler: log đủ stack trace, dialog luôn qua `Platform.runLater` (tránh `showAndWait` trong lúc layout)
- `navigation/StartupErrorReporter` — ghép nội dung dialog từ `messages_vi.properties`; lỗi không mong muốn không hiện chữ thô
- Sửa `AppException.java` (19 dấu `.` thừa làm `mvn clean compile` fail), thứ tự import `App.java`, comment bị ngắt dòng
- Review: agy lần 1 (7 điểm) + Claude lần 2 → nhận 5, nhận một phần 2, hoãn `initOwner` sang M2

**File thay đổi:** `App.java`, `AppConfig.java`, `AppPaths.java`, `AppException.java`, `navigation/StartupErrorReporter.java`, `i18n/messages_vi.properties`, test `StartupErrorReporterTest` (7), `MessagesBundleTest` (4)

**Đã kiểm chứng (Claude chạy):**
- `mvn -B clean verify` → Tests run: 13, Failures: 0, Errors: 0 → BUILD SUCCESS
- `mvn spotless:check` → pass
- **Chưa kiểm chứng:** chạy app thật (`mvn javafx:run`) với: sai mật khẩu DB, chưa có `config.properties`, Postgres tắt → phải thấy dialog tiếng Việt rồi app thoát

**Ghi chú vận hành agy (đã học được):**
- Phải chỉ định `--model gemini-3.8-flash-high` — model mặc định trả 404, Gemini 4 Hydrogen không có trong tài khoản
- Headless tự từ chối mọi lệnh shell không có trong allow; allow phải **khớp nguyên văn** (`command(mvn -q -B test)`), truyền qua `--settings` chỉ cho phiên đó
- Tạo sẵn thư mục mới trước khi gọi agy (nó hay `mkdir` và bị chặn)
- `GEMINI.md` bị agy cắt ở 24.000 byte (file 51.557 byte) — mục 6–11 có thể không được đọc; cần bản rút gọn
- agy có quyền đọc toàn cục `C:\programming\java project` (grant cũ từ dự án warehouse-backend)

**Bước tiếp theo:** GĐ2 — `models/entity/Customer`, `repositories/CustomerRepository`, `di/ServiceRegistry`

---

## [2026-11-24] — Rà soát dự án; đổi quy trình sang agy viết code (ADR-29)
**Milestone:** M1 (đang làm dở)

**Đã làm (ghi bù phần M1 từ các commit `3c26bbc`, `592e0d9`):**
- `config/AppConfig` — gộp `application.properties` + `%APPDATA%/SalesManager/config.properties`; lần chạy đầu tạo file từ template rồi ném `AppException`
- `config/DataSourceFactory` (HikariCP), `config/DatabaseMigrator` (Flyway), gọi trong `App.init()`; `App.stop()` đóng pool
- `V1__init.sql`: `province`, `ward`, `customer_group`, `sales_rep`, `customer`, `app_setting`, trigger `updated_at`, seed 3 setting doanh số + 34 tỉnh
- ADR-26 (PG 18 port 5432), ADR-27 (collation `vi-VN-x-icu` theo cột), ADR-28 (seed tỉnh, phường để trống)
- Session này: rà toàn bộ dự án, ADR-29 + mục 0.1a trong `CLAUDE.md`/`GEMINI.md`; `config.properties.template` sửa PG16 → 18

**File thay đổi (session này):**
- `CLAUDE.md`, `GEMINI.md` — mục 0.1a, mục 11
- `docs/DECISIONS.md` — ADR-29
- `src/main/resources/config.properties.template` — đường dẫn `pg_dump` bản 18

**Đã kiểm chứng:**
- Chưa kiểm chứng: `mvn spotless:check verify` và `mvn javafx:run` sau `592e0d9` — chưa có kết quả dán lại; app có kết nối được PG 18 + Flyway chạy V1 thành công hay chưa cũng chưa rõ

**Còn dang dở / Bug đã biết:**
- `App.java` import sai thứ tự (hai khối `com.tuantu.salesapp.*`) → nhiều khả năng `spotless:check` fail
- Lỗi config/DB/migration ở `init()` chỉ in stack trace thô, chưa có dialog rồi thoát; chưa có uncaught exception handler
- Comment bị IDE ngắt dòng trong `App`, `AppConfig`, `AppPaths`
- File rác `potless:check` đã bị commit → `git rm "potless:check"`
- `pom.xml` comment còn ghi "PostgreSQL 16" (sửa khi có task pom tiếp theo); driver 42.7.3 với PG 18 chưa kiểm chứng
- Spec: hai mục cùng số 9.1; spec 9.3 có màu 🟡 trong khi `CLAUDE.md` chỉ cho 3 màu — cần chốt trước M2
- `samples/` vẫn nằm trong dự án → **chặn** việc gọi agy

**Bước tiếp theo — M1 theo ADR-29:**
1. GĐ1: báo lỗi khởi động bằng dialog + thoát, uncaught handler, sửa import/comment
2. GĐ2: `models/entity/Customer`, `repositories/CustomerRepository`, `di/ServiceRegistry`
3. GĐ3: `TestDatabase` có guard `_test`, `CustomerRepositoryTest`, `AppConfigTest`

---

## [2026-10-02] — M0.3 xong: khung chạy được, và áp house style
**Milestone:** M0.3 ✅ — kèm ADR-24, ADR-25

**Đã làm:**
- `pom.xml`: Java 21, JavaFX 21.0.2, AtlantaFX, Ikonli, SLF4J+Logback, PostgreSQL driver, HikariCP, Flyway (cả `flyway-database-postgresql`), JUnit5+Mockito+AssertJ, Spotless+palantir, `javafx-maven-plugin`. Jar + `lib/` gom ở `target/app/` theo ADR-24, không fat jar
- `Launcher` tách `App` — tránh "JavaFX runtime components are missing"
- `config/AppPaths` + `exceptions/AppException`: mọi dữ liệu người dùng dưới `%APPDATA%/SalesManager/`
- `logback.xml` rolling theo size và ngày, `totalSizeCap=200MB`, UTF-8; đọc thư mục log từ system property `salesmanager.logDir` do `Launcher.main()` đặt **trước** lần gọi `LoggerFactory` đầu tiên
- `application.properties` + `config.properties.template` (mật khẩu DB không nằm trong jar)
- AtlantaFX `PrimerLight` + `ui/styles/main.css`
- **ADR-25**: áp house style cho naming/comment/package — package số nhiều (`exceptions`, `repositories`, `services`, `utils`, `models/{dto,entity}`), FXML snake_case, `@FXML` tiền tố loại, `handleXxx`/`goXxx`, không Javadoc, một `main.css`, thứ tự import theo khối. Giữ ADR-2 cho kiến trúc (SLF4J, `Task`, DI, Java 21)
- **ADR-24**: `jlink` chỉ gói runtime JDK+JavaFX; app chạy classpath, jar + `lib/`

**File thay đổi:**
- `pom.xml` — toàn bộ build; thêm `<importOrder>` cho Spotless để giữ nhóm import của ADR-25
- `src/main/java/.../Launcher.java`, `App.java`, `config/AppPaths.java`, `exceptions/AppException.java`
- `src/test/java/.../config/AppPathsTest.java`
- `src/main/resources/logback.xml`, `application.properties`, `config.properties.template`, `com/tuantu/salesapp/ui/styles/main.css`
- `CLAUDE.md` mục 3, 5, 7, 14, 16; `docs/DECISIONS.md` (ADR-24, ADR-25); `docs/ROADMAP.md`; `.claude/settings.json`

**Đã kiểm chứng:**
- `mvn clean compile` → BUILD SUCCESS
- `mvn spotless:check verify` → **Tests run: 2, Failures: 0, Errors: 0** → BUILD SUCCESS
- `mvn javafx:run` → cửa sổ mở, console sạch, **exit code 0** khi đóng (không thread nào treo lại)
- `%APPDATA%/SalesManager/` có `logs`, `backup`, `attachments`; `app.log` ghi được và **append** qua nhiều lần chạy
- **Không** có thư mục `logs/` lạc vào dự án → system property đặt đúng thời điểm
- Chữ "Sales Manager" to và đậm → `main.css` được nạp đúng đường dẫn
- 3 version trước đây chưa chắc đã được xác nhận có thật: `javafx-maven-plugin 0.0.8`, `spotless 2.43.0`, `palantir-java-format 2.47.0`

**Còn dang dở / đã biết:**
- **Chưa kiểm chứng được AtlantaFX bằng mắt** — màn hình mới chỉ có một `Label`, không có control nào để theme tô. Sẽ rõ ở M2 khi có sidebar, nút, `TableView`
- IDE vẫn đang tự ngắt comment ở ~100 cột (thấy dòng `// break.` đứng một mình trong `App.java`). Spotless không rewrap comment nên lỗi này tích tụ dần — cần tắt auto-wrap, đặt ruler 120
- `ServiceRegistry` **hoãn sang M1**: ở M0.3 chưa có service nào để lắp, tạo class rỗng là placeholder mà `CLAUDE.md` mục 11 cấm
- File rác `potless:check` ở root (output `git diff` do bấm `s` trong pager) — xoá bằng `del "potless:check"`

**Bước tiếp theo — M1:**
1. Cài PostgreSQL 16, tạo database `salesmanager` và `salesmanager_test`
2. `config/DataSourceFactory` + HikariCP, đọc mật khẩu từ `%APPDATA%/SalesManager/config.properties`
3. Flyway chạy **khi khởi động, trước khi hiện màn hình chính**; lỗi → báo rõ và thoát
4. `V1__init.sql`: `province`, `ward`, `customer_group`, `customer`, `sales_rep`, `app_setting`
5. `di/ServiceRegistry` (lúc này mới có việc thật)
6. `CustomerRepository` + integration test trên `salesmanager_test`, có **guard** fail nếu tên DB không kết thúc `_test`

---

## [2026-10-02] — Chốt quyết định, dựng khung tài liệu dự án
**Milestone:** M0.1 + M0.2 — **xong**

**Đã làm:**
- Đọc spec v2 và bản mẫu `CLAUDE-JavaFX.md`, lập roadmap và chia giai đoạn
- Chốt toàn bộ quyết định nền tảng (D1–D6, A1–A12, B1–B5, C1–C7, Q1–Q9) → **23 ADR**
- Phát hiện và giải quyết xung đột giữa house style Java cá nhân và `CLAUDE.md` (6 điểm → ADR-2)
- Bổ sung 11 thay đổi mô hình dữ liệu so với spec v2 (Δ1–Δ11), trong đó quan trọng nhất:
  - Khách hàng có **hai trục** phân loại: kênh bán (quyết định bảng giá) và hạng A/B/C
  - **Đơn vị đóng gói** thùng/lẻ — spec v2 thiếu hoàn toàn
  - Import đổi từ "skip nếu đã tồn tại" sang **upsert + `row_hash` + batch theo kỳ**
  - Công nợ **hai trục**: hạn hợp đồng và ngày khách hẹn trả
  - **Nhiều sales rep** + tính năng mới: theo dõi hiệu suất và dự báo rep sắp fail target
  - **Sức khỏe khách hàng**: lệch nhịp mua hàng + rụng SKU
- `git init`
- Viết `CLAUDE.md` thật (điền hết placeholder, thêm mục 0.2, 0.3, 6.5)
- Viết spec **v3** (bản v2 lưu ở `docs/archive/`)
- Viết `docs/ROADMAP.md`, `docs/DECISIONS.md`, `docs/PROGRESS.md`
- Viết `.gitignore` (chặn `samples/`, `backup/`, `*.xlsx`, `config.properties`)
- Viết `.claude/settings.json` (chặn Claude sửa `.java`/`.fxml`/`.css` và đọc `samples/`, `backup/`)
- Viết 8 slash command trong `.claude/commands/`

**File thay đổi:**
- `CLAUDE.md` — mới, thay bản mẫu `CLAUDE-JavaFX.md`
- `sales-app-spec-javafx.md` — v2 → **v3**
- `docs/ROADMAP.md`, `docs/DECISIONS.md`, `docs/PROGRESS.md` — mới
- `docs/archive/spec-v2-2026-10-02.md`, `docs/archive/CLAUDE-template-original.md` — bản gốc lưu lại
- `.gitignore`, `.claude/settings.json`, `.claude/commands/*.md` (8 file) — mới

**Đã kiểm chứng:**
- Chưa có gì để build — chưa có `pom.xml`. Không có lệnh nào cần chạy ở milestone này.

**Còn dang dở / việc cần chuẩn bị:**
- **Chặn M5a:** mẫu 5 file Excel (đơn hàng, hóa đơn, công nợ, hàng trả về, tồn kho) đặt trong `samples/`
- **Chặn M1 + M7:** danh sách 5–10 sales rep (mã + tên) và chỉ tiêu công ty giao cho từng rep
- **Chặn M12:** một ví dụ thật cho mỗi dạng khuyến mãi (5 dạng)
- **Chặn M12b:** danh sách SKU bắt buộc (MSL) theo từng kênh khách
- Chưa cài / chưa xác nhận: PostgreSQL 16 trên máy, `pg_dump` trong `PATH`, BitLocker đã bật
- `CLAUDE-JavaFX.md` ở root còn dư — xoá được sau khi đối chiếu xong (bản gốc đã lưu trong `docs/archive/`)

**Bước tiếp theo — M0.3 (task LARGE, cần plan + "go"):**
1. `pom.xml`: Java 21, JavaFX 21.0.2, PostgreSQL driver, HikariCP, Flyway, POI, OpenPDF, AtlantaFX, Ikonli, SLF4J+Logback, JUnit5+Mockito+AssertJ, Spotless+palantir, `javafx-maven-plugin`
2. `Launcher.java` tách `App.java`
3. `config/AppPaths` — `%APPDATA%/SalesManager/`
4. `logback.xml` — ghi file có rolling, không log dữ liệu khách hàng
5. `config.properties` sinh lần chạy đầu, hỏi mật khẩu DB
6. Cửa sổ trống mở được → `mvn verify` + `mvn javafx:run`

**Commit message đề xuất:**
```
docs: add project rules, spec v3, roadmap and 23 ADRs
```
