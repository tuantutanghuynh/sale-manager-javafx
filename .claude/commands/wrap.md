---
description: Tổng kết session, soạn nội dung cho PROGRESS.md
---
Tổng kết session.

1. Chạy `git status` và `git diff --stat` (chỉ đọc) để biết thực tế đã thay đổi gì.
2. Soạn một mục mới cho `docs/PROGRESS.md`, **đặt lên trên cùng**, theo mẫu:

~~~markdown
## [YYYY-MM-DD] — [Tiêu đề ngắn]
**Milestone:** [Mx]
**Đã làm:**
- ...
**File thay đổi:**
- `path/to/file` — mô tả ngắn
**Đã kiểm chứng:**
- [lệnh đã chạy + kết quả tôi dán lại; chưa có thì ghi "chưa kiểm chứng"]
**Còn dang dở / Bug đã biết:**
- ...
**Bước tiếp theo:**
- ...
~~~

3. Có quyết định thuộc trường hợp ở CLAUDE.md mục 12 → soạn luôn ADR cho `docs/DECISIONS.md`.
4. Đề xuất commit message theo Conventional Commits (tiếng Anh). Không tự chạy `git commit`.
