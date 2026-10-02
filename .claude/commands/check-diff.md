---
description: Review các thay đổi chưa commit
---
Chạy `git status` và `git diff` (lệnh chỉ đọc), rồi review theo thứ tự:

1. **Phạm vi** — có file nào thay đổi ngoài plan không? Báo dạng
   `Files changed: N | Expected: M | Unexpected: K`
   Có file ngoài dự kiến → nêu tên và hướng dẫn hoàn tác phần đó.
2. **Đúng/sai** — bug logic, biên chưa xử lý, null, chia cho 0, `BigDecimal` thiếu scale hoặc `RoundingMode`, lệch múi giờ, cuối tháng / cuối năm.
3. **Phân lớp** (CLAUDE.md mục 6) — SQL hoặc business logic trong controller? `import javafx.*` trong service? Singleton tĩnh thay cho DI?
4. **Tiền & thời gian** — còn `double`/`float` cho tiền? Còn `java.util.Date` hay `SimpleDateFormat`?
5. **Security** (mục 8) — SQL nối chuỗi, đường dẫn file chưa `normalize()`, secret bị lộ, log chứa dữ liệu khách hàng.
6. **Thiếu test** — logic mới nào chưa có test? Công thức tuổi nợ / doanh số / tiến độ chỉ tiêu đã có test biên chưa?
7. **Rác** — `System.out.println`, `printStackTrace()`, code comment-out, `TODO` không giải thích.

Mỗi mục: **ĐẠT / CHƯA ĐẠT** kèm `path/to/File.java:dòng`. Không sửa code, chỉ báo cáo và đề xuất cách sửa.
