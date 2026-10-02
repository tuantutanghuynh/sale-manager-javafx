---
description: Phân tích lỗi, viết test tái hiện trước, rồi mới hướng dẫn sửa
---
Lỗi: $ARGUMENTS

Thứ tự bắt buộc, không được đảo:

1. **Đọc toàn bộ stack trace**, đặc biệt dòng `Caused by:` ở cuối. Nêu **nguyên nhân gốc** trước khi nói tới cách sửa. Đối chiếu bảng "Lỗi hay gặp" ở CLAUDE.md mục 7 trước (`Not on FX application thread`, `LoadException`, `@FXML` null, `JavaFX runtime components are missing`...).
2. **Đọc code thật** ở chỗ gây lỗi. Không đoán nội dung file chưa đọc.
3. **Hướng dẫn viết test tái hiện lỗi trước.** Tôi chạy, xác nhận test **FAIL** đúng như mô tả. Chỉ sau đó mới sang bước 4.
4. **Hướng dẫn sửa** theo cấu trúc 4 phần ở mục 0.1.
5. Tôi chạy lại test, mong đợi PASS.

Cấm: khuyên xoá hoặc `@Disabled` hoặc sửa assertion cho test pass. Cấm bọc `Platform.runLater`, `catch (Exception e) {}` rỗng, `@SuppressWarnings`, `Thread.sleep` để né lỗi.

Sửa 2 lần cùng một hướng mà vẫn lỗi → **dừng lại**, trình bày giả thuyết và hỏi tôi.
