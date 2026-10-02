---
description: Phân tích và lập plan cho một task, KHÔNG sửa file
---
Task: $ARGUMENTS

Chỉ phân tích và lập plan. Không tạo/sửa bất kỳ file nào.

1. Đọc `docs/PROGRESS.md` để biết đang ở milestone nào.
2. Đọc các file liên quan trực tiếp tới task (controller + FXML + service + repository). Không lập plan cho file chưa đọc.
3. Phân loại task theo CLAUDE.md mục 4.1 và **ghi rõ mức SMALL / MEDIUM / LARGE** ở dòng đầu. Không chắc → chọn mức cao hơn.
4. Trình bày:
   - Tóm tắt task bằng 2–3 câu để xác nhận hiểu đúng
   - Danh sách file sẽ tạo / sửa, mỗi file một dòng kèm lý do
   - Các bước đánh số, mỗi bước là một thứ chạy được
   - Rủi ro và cách rollback (bắt buộc nếu có migration, đổi threading, đổi `pom.xml`, đổi nơi lưu dữ liệu)
   - Test sẽ viết / sẽ chạy
   - Có thuộc trường hợp phải ghi ADR (CLAUDE.md mục 12) không
5. Kết thúc bằng: Chờ xác nhận, gõ `/go` để nhận hướng dẫn thực hiện.
