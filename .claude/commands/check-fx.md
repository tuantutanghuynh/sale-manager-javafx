---
description: Rà soát đặc thù JavaFX cho phần code vừa làm
---
Đọc các file vừa thay đổi (`git diff`) và kiểm tra theo CLAUDE.md mục 7:

1. Có việc nặng (DB / file / mạng / tính toán lâu / `Thread.sleep`) nào chạy trên FX Application Thread không?
2. Có chỗ nào đụng vào `Node` / `Scene` / property đang bind với UI từ thread nền không?
3. Có `new Thread(...)` nào không đi qua executor dùng chung trong `task/` không?
4. Có listener nào gắn vào object sống lâu (service, model dùng chung) mà chưa được gỡ khi đóng view không?
5. Controller có chứa SQL hoặc business logic không? Service có `import javafx.*` không?
6. Có chuỗi hiển thị bị hardcode (phải nằm trong `messages_vi.properties`), hoặc `setStyle(...)` inline không cần thiết không?
7. Màn hình đã xử lý đủ 4 trạng thái **loading / error / empty / success** chưa?
8. Khi việc nền đang chạy: nút đã bị disable để chống bấm lặp chưa? Có hiển thị lỗi cho người dùng khi thất bại chưa?
9. Có kích thước pixel cứng thay vì `HGrow`/`VGrow`/`Priority`? Cửa sổ chính đã có `minWidth`/`minHeight`?

Mỗi mục: **ĐẠT / CHƯA ĐẠT** kèm `path/to/File.java:dòng`. Không sửa code, chỉ báo cáo và đề xuất cách sửa.
