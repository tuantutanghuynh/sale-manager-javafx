# ROADMAP — Sales Manager

Cập nhật: 02/10/2026. Nguồn yêu cầu: `sales-app-spec-javafx.md` v3. Quyết định: `docs/DECISIONS.md`. Tiến độ thực tế: `docs/PROGRESS.md`.

**Nguyên tắc chia milestone:** mỗi milestone kết thúc bằng **một thứ mở app lên dùng được và có test**. Không có milestone nào chỉ là hạ tầng mà không thấy gì trên giao diện, trừ M0–M1.

---

## Giai đoạn 0 — Khung dự án

| ID | Nội dung | Xong khi | Ước |
|---|---|---|---|
| **M0.1** | Chốt quyết định, ghi ADR | 25 ADR có mặt trong `docs/DECISIONS.md` | ✅ xong |
| **M0.2** | `git init`; `CLAUDE.md`; `docs/`; `.gitignore`; `.claude/settings.json` + 8 slash command | `git status` sạch, `/plan-task` gọi được | ✅ xong |
| **M0.3** | `pom.xml`; `Launcher` tách `App`; `config/AppPaths`; `logback.xml` ghi file có rolling; `config.properties` sinh lần chạy đầu; cửa sổ trống mở được | `mvn verify` pass; app mở; có file log trong `%APPDATA%/SalesManager/logs/` | ✅ xong |

> M0.3 là task **LARGE** (`pom.xml` + dependency + nơi lưu dữ liệu người dùng). Cần plan riêng + "go".

---

## Giai đoạn 1 — MVP

| ID | Nội dung | Bảng DB mới | Test bắt buộc | Ước (tuần) |
|---|---|---|---|---|
| **M1** | Hikari + Flyway chạy khi khởi động **trước** màn hình chính (lỗi → báo rõ và thoát); `V1__init.sql`; seed `province`/`ward`; `sales_rep`; `CustomerRepository` | `province`, `ward`, `customer_group`, `customer`, `sales_rep`, `app_setting` | Repository integration test chạy qua migration thật trên `salesmanager_test`; **guard** fail nếu tên DB không kết thúc `_test` | 1–1.5 |
| **M2** | Khung UI: sidebar trái + nội dung phải, `SceneNavigator`, `DialogService`, `FxmlLoaderFactory`, AtlantaFX sáng/tối; **chốt nền tảng thiết kế** (thang 8px, thang cỡ chữ, bảng màu, 3 màu trạng thái) trong `ui/styles/main.css`; `messages_vi.properties`; **màn hình Danh sách khách hàng** với `FilteredList`, lọc kênh/tỉnh/phường/hạng/rep, đủ 4 trạng thái | — | TestFX: "mở app → thấy danh sách" | 1.5 |
| **S1** | **Spike `jpackage`** một lần cho sớm — chỉ cần ra file `.msi` mở được | — | Thủ công | 0.5 ngày |
| **M3** | Hồ sơ khách (tab doanh số/công nợ để rỗng, điền ở M6–M7); Cài đặt kênh khách + bảng giá + chính sách nợ + **ngày nhắc tự đặt**; danh sách rep + gán khách cho rep; `utils/Validator` | `product`, `product_group`, `price_tier` | Unit test Validator; "khách để trống → lấy chính sách của kênh"; "khách có giá trị riêng → ghi đè" | 1 |
| **M4** | ⚠️ **Backup + restore** — `pg_dump` khi đóng app, giữ N bản, restore được. **Phải xong trước khi nhập dữ liệu thật** | — | Backup → restore sang `salesmanager_test` → so số dòng từng bảng | 0.5 |
| **M5a** | **Máy import dùng chung:** ánh xạ cột lưu trong DB, xem trước, lỗi từng dòng, một transaction, **upsert + `row_hash` + batch theo kỳ**, hủy lô, chạy nền hủy được | `import_batch`, `import_column_mapping` | 7 test ở spec mục 9.1 — **đây là bộ test quan trọng nhất của cả giai đoạn 1** | 1.5 |
| **M5b** | Import **đơn hàng** + sản phẩm + quy đổi thùng/lẻ | `sales_order`, `order_item` | Quy đổi thùng↔lẻ khi file ghi lẫn hai đơn vị; `units_per_pack_at_sale` giữ nguyên khi đổi quy cách | 1 |
| **M6** | Import **hóa đơn + công nợ + thanh toán**; **màn hình Công nợ hai trục tuổi nợ**; nhập `promised_date` và lịch sử hẹn trả | `invoice`, `receivable`, `payment`, `collection_followup` | Tuổi nợ ở biên: đúng ngày đến hạn, cuối tháng, trả một phần, trả vượt; tuổi nợ theo `promised_date` | 1.5 |
| **M6b** | Import **hàng trả về**; doanh số net = bán − trả về (bật/tắt) | `sales_return`, `sales_return_item` | Doanh số có và không trừ trả về | 0.5 |
| **M7** | Doanh số theo kỳ × khách × kênh × SKU × **rep**; `kpi_target` một bảng; quy tắc "chỉ tiêu cụ thể nhất thắng" | `kpi_target` | Doanh số cả hai cơ sở ngày; làm tròn `BigDecimal`; chỉ tiêu tổng vs chi tiết không cộng dồn | 1.5 |
| **M7b** | **Hiệu suất Sales:** `period_pacing_curve`, `pace_index`, `projected_final` / `gap` / `uplift_needed`, phiếu điểm 8 chỉ số | `rep_performance`, `period_pacing_curve` | `pace_index` với đường thẳng và với đường cong lịch sử; biên đầu kỳ (chia 0) và cuối kỳ | 1.5 |
| **M8** | Dashboard JavaFX Charts: doanh số vs chỉ tiêu, tuổi nợ, top khách, cơ cấu kênh, hiệu suất rep; **Việc hôm nay v1** | — | Service của dashboard test được không cần UI | 1 |
| **M8b** | **Sức khỏe khách hàng:** lệch nhịp (trung vị), rụng SKU, xếp ưu tiên theo hạng → `customer_health`; Việc hôm nay đọc từ đó | `customer_health` | Lệch nhịp với khách nhịp 2 tuần và nhịp 1 tháng; rụng SKU; ngưỡng doanh số nền lọc được khách bé | 1 |
| **M9** | Báo cáo Excel (POI) + PDF (OpenPDF), chọn kỳ và bộ lọc, chạy nền có tiến trình và hủy được | — | File sinh ra mở được; số tổng khớp service | 1 |
| **M10** | `jlink` + `jpackage`; **smoke test trên máy sạch không có JDK**; nâng cấp từ bản cũ, kiểm tra dữ liệu còn nguyên; dùng thật, sửa lỗi | — | Checklist smoke test thủ công | 1 |

**Tổng giai đoạn 1 ≈ 16–17 tuần ngoài giờ** (kể cả M0).

### Phương án cắt ngắn

Cần bản dùng được sớm hơn thì **hoãn M7b, M8b, M9 sang sau M10**. Bản đầu tiên cài dùng thật sau **≈11 tuần**, gồm: khách hàng, 5 loại import, công nợ hai trục, doanh số + chỉ tiêu, dashboard, backup, đóng gói. Hai tính năng phân tích thêm vào sau mà không phải sửa schema.

---

## Giai đoạn 2 — Tồn kho, khuyến mãi, viếng thăm

| ID | Nội dung | Bảng DB mới |
|---|---|---|
| **M11** | Import tồn kho theo lô; cảnh báo cận date theo tầng 90/60/30 ngày, ngưỡng riêng theo kênh khách | `stock_lot` |
| **M12** | Khuyến mãi 5 dạng: quy tắc + phạm vi + tiến độ từng khách × chương trình × kỳ | `promotion`, `promotion_rule`, `promotion_scope`, `promotion_progress`, `display_check`, `marketing_material_issue` |
| **M12b** | **MSL**: SKU bắt buộc theo kênh; tỉ lệ tuân thủ theo khách và theo rep (chỉ số 6 của phiếu điểm) | `msl_item` |
| **M13** | Gợi ý đẩy hàng: lô cận date → khách từng mua → kênh phù hợp | — |
| **M14** | Lịch viếng thăm, ghi chú, ảnh (`%APPDATA%/SalesManager/attachments/`, DB lưu đường dẫn) | `visit`, `reminder` |
| **M15** | Việc hôm nay bản đầy đủ: thêm hàng cận date + lịch thăm trong ngày | — |
| **M16** | *(tùy chọn)* Dashboard WebView + ECharts — **chỉ nếu** JavaFX Charts ở M8 chứng minh là không đủ | — |

> ⚠️ **M12 bị chặn** cho tới khi có một ví dụ thật cho mỗi dạng khuyến mãi (spec mục 11.5).

---

## Giai đoạn 3 — Phân tích

| ID | Nội dung |
|---|---|
| **M17** | Bộ mô phỏng khuyến mãi lãi/lỗ: hòa vốn, so với các đợt trước |
| **M18** | Phân khúc RFM cho dashboard |
| **M19** | Gợi ý khuyến mãi qua Claude API — chỉ gửi số liệu đã tổng hợp; API key trong cài đặt; mọi tính tiền vẫn do code làm |

---

## Đường găng

```
M0.3 ─→ M1 ─→ M2 ─→ M3 ─→ M4 ─→ M5a ─→ M5b ─→ M6 ─→ M6b ─→ M7 ─┬─→ M7b ─→ M8 ─→ M8b ─→ M9 ─→ M10
                │                          ↑                      │
                └─ S1 (spike jpackage)     │                      └─ cần ≥ 1 năm dữ liệu lịch sử
                                            │                         cho đường cong tiến độ
  mẫu 5 file Excel ────────────────────────┘
  danh sách rep + chỉ tiêu ───────────────────────────────────────→ M7
  ví dụ 5 dạng khuyến mãi ─────────────────────────────────────────→ chặn M12
  danh sách MSL ───────────────────────────────────────────────────→ chặn M12b
```

**Hai thứ cần chuẩn bị song song với M0–M3 để không phải chờ:** mẫu 5 file Excel, và danh sách rep + chỉ tiêu công ty giao.

**Ghi chú về M7b:** đường cong tiến độ cần ≥ 1 năm dữ liệu lịch sử. Nếu import được dữ liệu 12 tháng quá khứ ở M5b–M6 thì M7b dùng được ngay; nếu không, M7b chạy bằng đường thẳng kèm nhãn "ước lượng thô" và tự tốt lên khi đủ dữ liệu.

---

## Definition of Done cho mỗi milestone

Ngoài checklist `CLAUDE.md` mục 4.5:

- [ ] `mvn verify` pass (tôi chạy, dán kết quả — chưa có thì ghi "chưa kiểm chứng")
- [ ] `mvn spotless:check` pass
- [ ] Mở app thử tay: dữ liệu rỗng, dữ liệu rất dài, resize cửa sổ, Tab/Enter/Esc, bấm lặp nhanh — console không có exception
- [ ] Màn hình mới xử lý đủ 4 trạng thái loading / error / empty / success
- [ ] `/check-fx` không còn mục CHƯA ĐẠT
- [ ] Có migration thì đã thử trên **DB mới** và **DB của milestone trước**; đã backup trước khi migrate
- [ ] Mọi tiền dùng `BigDecimal`, mọi thời gian dùng `java.time`
- [ ] Công thức nghiệp vụ mới có test biên
- [ ] Một mục mới trong `docs/PROGRESS.md`; có quyết định thuộc `CLAUDE.md` mục 12 thì thêm ADR
- [ ] Một commit theo Conventional Commits, `git status` không có file trong `samples/` hay `backup/`

### Critical flows cần UI test

1. Mở app → thấy danh sách khách hàng *(M2)*
2. Import một file Excel → xem trước → xác nhận → số liệu xuất hiện ở Công nợ *(M6)*
3. Import lại đúng file đó → số liệu **không** nhân đôi *(M5a)*
4. Hủy một lô import → số liệu trở về như trước *(M5a)*
5. Mở Hiệu suất Sales → thấy rep nào đang chậm tiến độ *(M7b)*

---

## Sổ rủi ro

| Rủi ro | Ảnh hưởng | Cách xử lý |
|---|---|---|
| **Mất dữ liệu công nợ** | Nặng nhất | M4 **trước** M5; backup khi đóng app; **thử restore định kỳ**, không chỉ tin là backup chạy |
| Cấu trúc file Excel của công ty đổi | Import vỡ | Ánh xạ cột là **cấu hình trong DB** — sai cột thì sửa trong app, không sửa code |
| Cơ chế chống trùng làm sai (M5a) | Số liệu nhân đôi hoặc mất dòng đã sửa → mọi báo cáo sai | Bộ 7 test ở spec mục 9.1 phải pass trước khi sang M5b. Đây là mục nhạy cảm, mọi sửa về sau là task LARGE |
| Dự báo fail target báo đỏ toàn bộ | Tính năng thành vô dụng, bị tắt | Dùng đường cong lịch sử, không dùng đường thẳng (ADR-18); ngưỡng đổi được trong Cài đặt |
| Chưa đủ 1 năm dữ liệu cho đường cong | M7b kém chính xác | Import dữ liệu 12 tháng quá khứ ở M5b–M6; tạm dùng đường thẳng có nhãn cảnh báo |
| Cảnh báo "khách giảm mua" quá nhiều | Không ai đọc | Lọc theo ngưỡng doanh số nền + xếp ưu tiên theo hạng khách (spec 9.2 ④) |
| Đoán sai cấu trúc bảng khuyến mãi | Phải migration bảng đã có dữ liệu | **Chặn M12** cho tới khi có ví dụ thật |
| Phát hiện vấn đề đóng gói ở tuần cuối | Trễ | Spike S1 ngay sau M2 |
| Dữ liệu khách hàng + giá vốn trên máy cá nhân | Rủi ro nghề nghiệp | BitLocker bắt buộc; `samples/` và `backup/` gitignored và Claude bị chặn đọc; lên GitHub chỉ dùng dữ liệu giả |
| Test chạy vào DB thật | Mất dữ liệu | Guard trong code test: tên DB không kết thúc `_test` → fail ngay; `psql`/`dropdb`/`flyway:clean` bị deny trong `.claude/settings.json` |
