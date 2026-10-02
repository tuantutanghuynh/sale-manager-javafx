---
description: Đi qua checklist Definition of Done
---
Đi qua từng mục trong CLAUDE.md mục 4.5, báo **ĐẠT / CHƯA ĐẠT / N/A (lý do)**.

Quy tắc bắt buộc: kết quả `mvn verify`, test, lint là do **tôi chạy và dán lại**. Chưa có kết quả tôi dán thì ghi **chưa kiểm chứng**, không được ghi "pass". Không suy luận ra kết quả build.

Trước khi kết luận: chạy `git status` và `git diff --stat` (chỉ đọc) để đối chiếu phạm vi.

Kết thúc bằng:
- Tóm tắt: đã làm gì, file nào thay đổi
- Cách test thủ công trên app (thao tác + kết quả mong đợi)
- Việc còn lại / rủi ro còn mở
- Có cần ghi ADR vào `docs/DECISIONS.md` không (mục 12)
- Commit message đề xuất theo Conventional Commits
