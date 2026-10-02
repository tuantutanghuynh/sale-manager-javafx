---
description: Đưa hướng dẫn thực hiện plan vừa được duyệt
---
Thực hiện plan vừa được duyệt. $ARGUMENTS

Tuân thủ CLAUDE.md mục 0.1: với file Java / FXML / CSS, **không tự sửa** — chỉ viết hướng dẫn trong chat.

Mỗi bước gồm đúng 4 phần, theo thứ tự:

1. **Thư mục & file** — đường dẫn đầy đủ từ root. File sửa thì ghi rõ vị trí cần tìm (tên method/class, hoặc "ngay dưới import cuối"), kèm số dòng gần đúng. FXML: ghi rõ `fx:id` cần thêm. `pom.xml`: ghi rõ nằm trong thẻ nào.
2. **Giải thích** — tiếng Việt, kiểu đang giảng bài: bài toán → giải pháp → vì sao không chọn cách khác. Thuật ngữ tiếng Anh giải thích ngay lần đầu xuất hiện. Dùng ví dụ hoặc ẩn dụ cho khái niệm khó.
3. **Code mẫu** — code block đầy đủ, dòng đầu là comment ghi đường dẫn file. Sửa file có sẵn thì nêu rõ đoạn **tìm** và đoạn **thay bằng**, không dán lại cả file. Ghi rõ các `import` cần thêm.
4. **Test cần chạy** — lệnh cụ thể, chạy ở thư mục nào, kết quả mong đợi. Bước đổi UI phải có thêm thao tác thủ công trên app (`mvn javafx:run`): bấm gì, nhập gì, thấy gì. Bước cuối của task luôn có `mvn verify`.

Nhịp: task SMALL/MEDIUM (≤ 4 bước) đưa hết một lần. Task LARGE chia từng đợt 2–3 bước, **dừng chờ kết quả test** của đợt đó.

Không chắc về API của thư viện → nói rõ "không chắc" và chỉ cách kiểm tra. Không bịa class hay method.
