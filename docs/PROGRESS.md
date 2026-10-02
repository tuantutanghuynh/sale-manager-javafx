# PROGRESS — nhật ký session

Mới nhất ở trên cùng. Mẫu và quy tắc: `CLAUDE.md` mục 12. Soạn bằng `/wrap`.

Claude **không nhớ session trước** — file này là bộ nhớ dài hạn của dự án. Đọc nó ở đầu mỗi session.

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
