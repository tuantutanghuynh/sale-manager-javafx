# Đặc tả app quản lý Sales — Desktop (Java 21 + JavaFX 21)

**Phiên bản: v3, ngày 02/10/2026.** Thay thế v2 (bản v2 lưu ở `docs/archive/spec-v2-2026-10-02.md`).

Thay đổi chính so với v2: chốt PostgreSQL; thêm đơn vị đóng gói; tách hai trục phân loại khách; thêm nhiều sales rep và **theo dõi hiệu suất sales + dự báo fail target**; thêm **sức khỏe khách hàng**; đổi hẳn cơ chế chống trùng khi import; công nợ hai trục; thêm file hàng trả về. Chi tiết trong `docs/DECISIONS.md`.

---

## 1. Mục tiêu

Gom toàn bộ công việc của một Sales Manager vào **một app desktop duy nhất**, thay cho nhiều file Excel rời rạc:

- Theo dõi **doanh số, công nợ, tiến độ chỉ tiêu** trực quan, không cần ghi chú thủ công
- **Theo dõi hiệu suất từng sales rep** và cảnh báo sớm rep có nguy cơ không đạt chỉ tiêu
- **Phát hiện khách đang giảm mua** trước khi mất khách
- Quản lý khách hàng và lịch viếng thăm
- Theo dõi khuyến mãi công ty đang chạy và tiến độ của từng khách
- Theo dõi tồn kho, hạn dùng để có chính sách đẩy hàng
- **Xuất báo cáo khi cần**, không phải lưu file rời

Người dùng: **một mình tôi**, một máy. Dùng thật trong công việc. Tôi phụ trách **5–10 sales rep**.

## 2. Nguyên tắc thiết kế

1. Dữ liệu gốc (đơn hàng, hóa đơn, công nợ, tồn kho) nằm ở công ty. App **đọc và tổng hợp**, không thay thế hệ thống công ty.
2. Nhập dữ liệu chủ yếu bằng **import Excel**. Chỉ nhập tay những thứ công ty không có: viếng thăm, ghi chú, chỉ tiêu cá nhân, ảnh trưng bày, ấn phẩm marketing đã cấp, **ngày khách hẹn trả nợ**.
3. Một người dùng nên **không cần phân quyền, không cần server riêng, không cần đăng nhập**. Dữ liệu nằm trên máy nên cần **mã hóa ổ đĩa (BitLocker)** và **backup tự động**.
4. Mọi con số tiền tính bằng `BigDecimal`, **tuyệt đối không dùng `double`**.
5. Làm từng giai đoạn nhỏ, **mỗi milestone dùng được ngay**.
6. Mọi ngưỡng cảnh báo và mọi quy tắc tính toán có thể đổi → nằm trong `app_setting`, không hardcode.

## 3. Công nghệ

Xem bảng đầy đủ ở `CLAUDE.md` mục 2. Tóm tắt các quyết định đã chốt:

| Phần | Lựa chọn | Lý do |
|---|---|---|
| Ngôn ngữ | Java 21 (LTS) | Đã có nền Java |
| Giao diện | JavaFX 21 + FXML **viết tay** + AtlantaFX + Ikonli | Chuẩn desktop Java, giao diện hiện đại |
| Build | Maven, **classpath (không JPMS)** | Ít rắc rối `opens`, `jpackage` vẫn chạy được |
| Cơ sở dữ liệu | **PostgreSQL 18** chạy trên máy, port 5432 | Kiểu số chính xác, truy vấn tổng hợp mạnh, sau này đưa lên server được. Bản 18 vì máy đã cài sẵn và đang ở port mặc định (ADR-26) |
| Truy cập DB | **JDBC thuần + repository tự viết** + HikariCP | Báo cáo doanh số/công nợ là truy vấn tổng hợp — viết SQL thẳng dễ hơn ORM |
| Migration | Flyway | File SQL có đánh số |
| Excel | Apache POI | Đọc/ghi .xlsx |
| PDF | OpenPDF | Xuất báo cáo |
| Biểu đồ | JavaFX Charts (giai đoạn 1) | WebView + ECharts chỉ xét ở M16 |
| Test | JUnit 5 + Mockito + AssertJ; TestFX + Monocle từ M2 | — |
| DB cho test | PostgreSQL local, database **`salesmanager_test`** | Không cần Docker; có guard chặn nếu tên DB không kết thúc bằng `_test` |
| Đóng gói | `jlink` + `jpackage` → `.msi` | Chạy như app bình thường, chỉ Windows |

**Không dùng Spring Boot.** App desktop một người dùng không cần nó. Dùng Java thuần với phân lớp rõ ràng (mục 4).

### 3.1 Giao diện

Cái quyết định app đẹp hay xấu **không phải** Scene Builder hay viết FXML tay — hai cách sinh ra cùng một file FXML và cùng một giao diện. Cái quyết định là **CSS + theme + typography + khoảng cách**.

| Mục đích | Công cụ |
|---|---|
| Giao diện hiện đại, theme sáng/tối | **AtlantaFX** |
| Biểu tượng vector sắc nét (quan trọng khi Windows scale 125/150%) | **Ikonli** (Material Design Icons) |
| Dựng màn hình | **Viết FXML tay**; Scene Builder chỉ mở để xem trước |
| Biểu đồ | JavaFX Charts + CSS |
| Thành phần bổ sung | ControlsFX / TilesFX — **chỉ thêm khi chứng minh được là cần** |

**Nền tảng thiết kế, chốt một lần ở M2 và mọi màn hình sau dùng lại:**
- Thang khoảng cách 8px: 4 / 8 / 16 / 24 / 32
- Một thang cỡ chữ duy nhất
- Một bảng màu, khai báo bằng *looked-up color* ở `.root` (`-app-primary`, ...)
- **Màu trạng thái cố định:** 🔴 đỏ = quá hạn / nguy cơ fail target · 🟠 cam = sắp đến hạn / cận date / chậm tiến độ · 🟢 xanh = đạt

**Nguyên tắc giao diện:**
- Một thanh menu bên trái, nội dung bên phải; trang mở đầu là **"Việc hôm nay"**
- Bảng dữ liệu luôn có tìm kiếm, lọc, sắp xếp; số tiền **căn phải**, định dạng `1.234.567`; ngày `dd/MM/yyyy`
- Mỗi màn hình có data xử lý đủ 4 trạng thái: **loading / error / empty / success**
- Thao tác nặng (import, báo cáo) chạy nền kèm thanh tiến trình **và hủy được**; giao diện không bị đơ

**WebView + ECharts:** chỉ xét ở **M16 (giai đoạn 2)**, và chỉ cho đúng màn hình dashboard, nếu JavaFX Charts thật sự không đủ. Hạn chế đã biết: WebKit bản cũ, tốn bộ nhớ, khởi động chậm, phải tự bảo trì cầu nối Java↔JS, chữ trong WebView không đồng bộ giao diện với phần JavaFX còn lại. Không dựng cả app bằng WebView.

## 4. Kiến trúc

Package gốc: **`com.tuantu.salesapp`**. UI nhóm **theo tính năng**, các tầng còn lại nhóm **theo loại**. Cấu trúc thư mục đầy đủ: `CLAUDE.md` mục 3.

```
View (FXML + CSS) → Controller → Service → Repository → PostgreSQL
                                    ↑
                              Importer (Excel) → Repository
```

**Quy tắc phân lớp:** `ui` chỉ gọi `service`; `service` gọi `repository`; **không viết SQL hay tính tiền trong controller**; `service` **không import `javafx.*`**. Nhờ vậy logic nghiệp vụ test được mà không cần mở giao diện.

Dependency injection: constructor injection, dựng một lần trong `di/ServiceRegistry` gọi từ `App.start()`. **Không singleton tĩnh.**

Việc nền: `javafx.concurrent.Task` + **một executor dùng chung** trong `task/BackgroundExecutor` (daemon thread, đóng trong `Application.stop()`). Không rải `new Thread`.

## 5. Mô hình dữ liệu

### 5.1 Danh sách bảng

| Nhóm | Bảng |
|---|---|
| Danh mục địa giới | `province`, `ward` |
| Khách hàng | `customer_group`, `customer` |
| Sales rep | `sales_rep` |
| Sản phẩm | `product`, `product_group`, `price_tier` |
| Bán hàng | `sales_order`, `order_item`, `invoice` |
| Hàng trả về | `sales_return`, `sales_return_item` |
| Công nợ | `receivable`, `payment`, `collection_followup` |
| Tồn kho | `stock_lot` |
| Khuyến mãi | `promotion`, `promotion_rule`, `promotion_scope`, `promotion_progress`, `display_check`, `marketing_material_issue` |
| Chỉ tiêu | `kpi_target` (một bảng cho mọi tổ hợp — xem 5.3.6) |
| Phân tích (cache) | `customer_health`, `rep_performance`, `period_pacing_curve` |
| MSL (giai đoạn 2) | `msl_item` |
| Công việc | `visit`, `reminder` |
| Hệ thống | `import_batch`, `import_column_mapping`, `app_setting` |

### 5.2 Quy ước chung

- Tiền VND: `NUMERIC(15,0)`. Trong Java dùng `BigDecimal`, VND `setScale(0, RoundingMode.HALF_UP)`.
- Mã khớp với công ty, dùng làm khóa tự nhiên khi import, đều **UNIQUE**: `customer.code`, `sales_order.order_no`, `invoice.invoice_no`, `product.sku`, `sales_rep.code`.
- Ngày nghiệp vụ: `DATE` (không timezone). Dấu thời gian hệ thống: `TIMESTAMPTZ` lưu UTC, đổi sang giờ máy ở tầng hiển thị.
- Mọi bảng nghiệp vụ có `id`, `created_at`, `updated_at`. Foreign key luôn có index. Index theo `(customer_id, date)` cho đơn hàng và công nợ.
- Tên bảng **số ít**, snake_case.

### 5.3 Điểm thiết kế quan trọng

#### 5.3.1 Khách hàng có **hai trục phân loại độc lập**

Đây là chỗ v2 nhập nhằng. Hai thứ khác nhau, một khách có cả hai:

| Trục | Cột | Giá trị | Quyết định điều gì |
|---|---|---|---|
| **Kênh bán** | `customer.group_id` → `customer_group` | khách tỉnh, khách thành phố, đại lý, khách sỉ, hotel, MT, thương mại điện tử... | **Bảng giá** (`price_tier`) + chính sách công nợ |
| **Hạng** | `customer.rank` | A / B / C | Thứ tự ưu tiên khi xếp danh sách việc cần làm |

`customer.rank` **do công ty quy định** — nhập tay hoặc lấy từ file import, không phải app tự tính. App có thể **đề xuất** hạng theo doanh số để đối chiếu, nhưng giá trị chính thức là của công ty.

`customer_group` giữ chính sách mặc định: hạn mức nợ, kỳ hạn nợ, **số ngày nhắc trước hạn**, ngưỡng hạn dùng tối thiểu còn lại. Khách để trống thì lấy theo nhóm; khách có giá trị riêng thì ghi đè. **Mọi giá trị này do tôi tự đặt trong màn hình Cài đặt**, app không seed cứng nhóm nào.

#### 5.3.2 Địa giới hành chính 2 cấp

`customer.province_id` + `customer.ward_id` — tỉnh/thành và phường/xã, **không có cấp huyện**.

Hai bảng danh mục `province` và `ward` được seed từ danh sách chính thức, và giao diện dùng **dropdown**, không cho nhập chữ tự do. Lý do: nhập tự do thì "TP.HCM" / "Hồ Chí Minh" / "HCM" thành ba khu vực khác nhau và báo cáo theo tỉnh sẽ sai.

#### 5.3.3 Đơn vị đóng gói (thiếu ở v2)

Sản phẩm bán theo **thùng và lẻ**, và quy cách thùng **có thể đổi theo thời gian**.

```
product:     sku, name, product_group_id, base_uom, pack_uom, units_per_pack, cost_price
order_item:  ..., qty, uom, units_per_pack_at_sale, qty_base, unit_price, unit_cost
```

- `qty` + `uom` = số lượng **như file ghi** (3 thùng, hoặc 12 lẻ)
- `units_per_pack_at_sale` = quy cách thùng **tại thời điểm bán** — lưu lại giống cách lưu `unit_cost`, để đổi quy cách thùng không làm sai số liệu đơn cũ
- `qty_base` = đã quy đổi về đơn vị lẻ, dùng để **cộng số lượng** giữa các đơn ghi đơn vị khác nhau

Mọi chỉ tiêu theo số lượng tính trên `qty_base`.

#### 5.3.4 Nhiều sales rep

Tôi phụ trách 5–10 rep. Cần **hai quan hệ khác nhau**:

- `customer.sales_rep_id` — **ai phụ trách khách này** (dùng cho độ phủ, MSL, chất lượng công nợ)
- `sales_order.sales_rep_id` — **ai viết đơn này** (khách của rep A nhưng rep B viết thay là chuyện thường)

`sales_rep(code, name, is_active, joined_at, left_at)` — có rep nghỉ việc thì báo cáo kỳ cũ vẫn phải đúng, nên không xoá rep, chỉ đặt `is_active = false`.

Import chỉ nhận dòng có rep thuộc danh sách của tôi; dòng khác đưa vào mục "bỏ qua" kèm lý do.

#### 5.3.5 Công nợ: kỳ hạn hợp đồng ≠ thực tế đòi được

Trong ngành pet, công ty quy định 10 hoặc 30 ngày, nhưng việc đòi được tiền thực tế rất khác. Nên công nợ có **hai trục**:

```
receivable:           invoice_id, amount, paid_amount, due_date, promised_date, status
collection_followup:  receivable_id, followup_no, promised_date, promised_at, result, note
```

- `due_date` — kỳ hạn theo hợp đồng. Tuổi nợ theo trục này cho biết **đúng/sai hợp đồng**.
- `promised_date` — ngày khách **hẹn** trả. Tuổi nợ theo trục này cho biết **hôm nay cần gọi ai**.
- `collection_followup` — lịch sử hẹn: hẹn lần thứ mấy, hẹn ngày nào, có giữ lời không. Khách hẹn rồi hoãn 3 lần là một tín hiệu khác hoàn toàn với khách hẹn một lần rồi trả.

Còn nợ = `amount − paid_amount`. **Tuổi nợ không lưu thành cột**, tính từ `due_date` hoặc `promised_date` khi truy vấn.

#### 5.3.6 Chỉ tiêu: một bảng cho mọi tổ hợp

Chỉ tiêu có 3 chiều đối tượng (rep / khách / sản phẩm), 2 loại thước đo, 3 loại kỳ. Làm mỗi loại một bảng sẽ thành 6 bảng. Một bảng duy nhất, **`NULL` nghĩa là "tất cả"**:

```sql
kpi_target(
  id,
  rep_id       NULL,   -- NULL = chỉ tiêu toàn nhóm
  customer_id  NULL,   -- NULL = mọi khách
  product_id   NULL,   -- NULL = mọi sản phẩm
  metric,              -- AMOUNT | QTY_BASE
  period_type,         -- MONTH | QUARTER | YEAR
  period_start DATE,
  target_value NUMERIC(15,0),
  UNIQUE(rep_id, customer_id, product_id, metric, period_type, period_start)
)
```

Diễn đạt được mọi thứ công ty giao, kể cả tổ hợp: *"rep Hùng, SKU thức ăn mèo, tháng 10, 300 thùng"* = `rep_id=Hùng, product_id=SKU, metric=QTY_BASE, period_type=MONTH`.

**Quy tắc đo kết quả:** khi tồn tại cả chỉ tiêu tổng *và* chỉ tiêu chi tiết, app đo theo **chỉ tiêu cụ thể nhất có mặt**; chỉ tiêu tổng chỉ để tham khảo. **Không cộng dồn chi tiết lên tổng** — công ty thường giao tổng ≠ tổng các phần.

#### 5.3.7 Giá vốn

`product.cost_price` (hiện tại) và `order_item.unit_cost` (lúc bán). Lưu giá vốn lúc bán để lãi gộp đơn cũ không đổi khi giá vốn thay đổi. **Giá vốn là dữ liệu nhạy cảm:** không log, không gửi lên API ở giai đoạn 3.

#### 5.3.8 Bảng cache phân tích

`customer_health`, `rep_performance` và `period_pacing_curve` là **bảng cache**, làm mới sau mỗi lần import. Lý do: "Việc hôm nay" là trang mở đầu, tính tại chỗ thì mở app phải chờ. Dữ liệu trong ba bảng này **luôn dựng lại được từ dữ liệu gốc** — mất không sao, chỉ cần tính lại.

## 6. Chức năng theo giai đoạn

Chi tiết từng milestone, Definition of Done, đường găng, sổ rủi ro: **`docs/ROADMAP.md`**.

### Giai đoạn 1 — MVP (M0–M10, ≈16–17 tuần ngoài giờ)

| Chức năng | Chi tiết |
|---|---|
| Khách hàng | Danh sách, tìm kiếm, lọc theo kênh / tỉnh / phường / hạng / rep phụ trách; hồ sơ khách |
| Cài đặt kênh khách | Hạn mức, kỳ hạn nợ, số ngày nhắc trước hạn, ngưỡng cảnh báo, bảng giá theo kênh |
| Sales rep | Danh sách rep tôi phụ trách, gán khách cho rep |
| Import Excel | 5 loại file: đơn hàng, hóa đơn, công nợ, hàng trả về, tồn kho. Xem trước, báo lỗi từng dòng, **upsert chống trùng**, lưu lịch sử batch, hủy được cả lô |
| Công nợ | Còn nợ theo khách; tuổi nợ **hai trục** (theo hạn hợp đồng và theo ngày hẹn trả); quá hạn; vượt hạn mức; lịch sử hẹn trả |
| Doanh số | Theo tháng/quý/năm × khách × kênh × sản phẩm × **rep**; số đơn; so với chỉ tiêu |
| **Hiệu suất Sales** | Đường cong tiến độ, `pace_index`, **dự báo rep nào sắp fail target**, phiếu điểm 8 chỉ số (mục 9.3) |
| **Sức khỏe khách hàng** | Lệch nhịp mua hàng, rụng SKU, xếp ưu tiên theo hạng (mục 9.2) |
| Dashboard | Doanh số so chỉ tiêu, tuổi nợ, top khách, khách giảm mua, hiệu suất rep |
| Việc hôm nay | Nợ sắp đến hạn / quá hạn / đến ngày khách hẹn; khách thiếu chỉ tiêu; khách lệch nhịp; rep chậm tiến độ |
| Xuất báo cáo | Excel và PDF, chọn kỳ và bộ lọc, chạy nền |
| Backup | `pg_dump` tự động mỗi lần đóng app, giữ N bản gần nhất, restore được |

### Giai đoạn 2 — Tồn kho, khuyến mãi, viếng thăm (M11–M16)

- Import tồn kho theo lô; cảnh báo cận date theo tầng (90/60/30 ngày), ngưỡng riêng theo kênh khách
- Quản lý khuyến mãi (mục 7) và tiến độ từng khách
- **MSL (Must Stock List)**: danh sách SKU bắt buộc theo kênh khách, và tỉ lệ tuân thủ của từng khách / từng rep
- Gợi ý đẩy hàng: lô cận date → khách từng mua → kênh khách phù hợp
- Lịch viếng thăm, ghi chú, đính kèm ảnh (lưu trong `%APPDATA%/SalesManager/attachments/`, DB chỉ lưu đường dẫn)
- "Việc hôm nay" bản đầy đủ: thêm hàng cận date và lịch thăm trong ngày
- *(tùy chọn)* M16: dashboard WebView + ECharts, chỉ nếu JavaFX Charts không đủ

### Giai đoạn 3 — Phân tích (M17–M19)

- Bộ mô phỏng khuyến mãi lãi/lỗ (cần giá vốn): hòa vốn, so với các đợt trước
- Phân khúc khách RFM (Recency–Frequency–Monetary) cho dashboard
- Gợi ý khuyến mãi bằng AI (mục 9.4), chỉ làm khi đã có vài tháng dữ liệu

### Ngoài phạm vi

- **Điện thoại:** không có check-in GPS. Viếng thăm ghi tay.
- **Nhiều máy / nhiều người dùng:** dữ liệu nằm trên một máy.
- **Đăng nhập, phân quyền:** không có.
- **Quản lý kho, xuất hóa đơn:** việc của công ty.

## 7. Khuyến mãi (5 dạng công ty đang chạy)

| Dạng | Cách mô hình hóa |
|---|---|
| Chiết khấu % theo số lượng | `promotion_rule`: bậc thang (từ số lượng A giảm x%, từ B giảm y%), theo sản phẩm |
| Mua X tặng Y | `promotion_rule`: mua `buy_qty` tặng `free_qty`, có thể nhiều mức, theo sản phẩm |
| Thưởng doanh số quý/năm | `promotion_rule`: mốc doanh số theo kỳ + mức thưởng; app tính tiến độ và số còn thiếu |
| Thưởng trưng bày | `display_check`: ngày kiểm tra, ảnh, đạt/không đạt, mức thưởng |
| Ấn phẩm marketing | `marketing_material_issue`: ấn phẩm, số lượng, ngày cấp, khách nhận |

- `promotion`: mã, tên, loại, ngày bắt đầu/kết thúc, ghi chú điều kiện
- `promotion_scope`: áp dụng cho kênh khách hoặc từng khách, và danh sách sản phẩm
- `promotion_progress`: tiến độ **từng khách × từng chương trình × từng kỳ**
- Tính tiến độ nằm trong `service`, có test riêng cho từng dạng

> ⚠️ **M12 bị chặn** cho tới khi có **một ví dụ thật cho mỗi dạng** (mục 11). Cấu trúc `promotion_rule` không chốt được mà không có ví dụ — đoán sai thì phải migration lại bảng đã có dữ liệu.

## 8. Màn hình

| Màn hình | Nội dung |
|---|---|
| **Việc hôm nay** (trang mở đầu) | Danh sách cần làm, xếp theo hạng khách × mức khẩn: đòi nợ (theo hạn và theo ngày hẹn), bơm đơn cho khách lệch nhịp, rep chậm tiến độ, hàng cận date, lịch thăm |
| Dashboard | Doanh số so chỉ tiêu theo tháng/quý/năm; tuổi nợ; top khách; cơ cấu theo kênh; hiệu suất rep |
| Khách hàng | Bảng + hồ sơ: doanh số các kỳ, công nợ, sức khỏe (lệch nhịp, rụng SKU), lịch sử thăm, khuyến mãi đang áp dụng |
| Công nợ | Bảng theo khách; **hai trục tuổi nợ**; quá hạn; vượt hạn mức; chi tiết hóa đơn; lịch sử hẹn trả |
| Doanh số | Phân tích theo kỳ × kênh × sản phẩm × khách × rep |
| **Hiệu suất Sales** | Bảng rep × chỉ tiêu: % đạt, `pace_index`, dự báo về đích, số còn thiếu, cần tăng bao nhiêu %; phiếu điểm 8 chỉ số |
| Khuyến mãi | Danh sách chương trình, tiến độ từng khách, khách sắp đạt / chưa đạt |
| Tồn kho | Theo lô, cận date, gợi ý đẩy |
| Viếng thăm | Lịch, ghi chú, ảnh |
| Import | Chọn loại file, ánh xạ cột, xem trước, sửa lỗi, xác nhận, lịch sử batch, hủy lô |
| Báo cáo | Chọn loại, kỳ, bộ lọc; xuất Excel/PDF |
| Cài đặt | Kênh khách, bảng giá, chỉ tiêu, danh sách rep, ngưỡng cảnh báo, đường cong tiến độ, đường dẫn backup |

## 9. Quy tắc nghiệp vụ

### 9.1 Doanh số và công nợ

**Doanh số** — mọi tham số nằm trong `app_setting`, app hỗ trợ **cả hai/cả ba** phương án để linh động:

| Tham số | Giá trị | Mặc định |
|---|---|---|
| Cơ sở ngày | `ORDER_DATE` hoặc `INVOICE_DATE` | cả hai đều xem được; mặc định `INVOICE_DATE` |
| Trừ hàng trả về | có / không | **có** |
| Trước hay sau VAT | `EXCL_VAT` / `INCL_VAT` | **`INCL_VAT`** (sau VAT) |

`order_item` lưu đủ `vat_rate`, `amount_excl_vat`, `vat_amount`, `amount_incl_vat` — lưu cả ba để sau này cần so trước VAT vẫn có, không phải import lại.

**Công nợ:** còn nợ = `amount − paid_amount`. Tuổi nợ chia tầng **0–30 / 31–60 / >60 ngày**, tính theo **cả hai trục** `due_date` và `promised_date` (mục 5.3.5).

**Hạn mức và kỳ hạn:** theo kênh khách (`customer_group`), có ngoại lệ từng khách.

**Nhắc nợ:** nhắc trước hạn N ngày — **N do tôi tự đặt**, mặc định theo kênh, ghi đè được theo từng khách. Quá hạn thì nhắc mỗi ngày.

**Thiếu chỉ tiêu:** so % đạt được với % **tiến độ lẽ ra phải đạt** tại thời điểm hiện tại, không phải với % thời gian đã trôi theo lịch — xem 9.3.

### 9.2 Sức khỏe khách hàng ("khách giảm mua")

Bốn tín hiệu, xếp theo mức hữu dụng. Mọi ngưỡng nằm trong `app_setting`.

#### ① Lệch nhịp mua hàng — tín hiệu tốt nhất

Mỗi khách có nhịp riêng (hotel đặt 2 tuần/lần, đại lý tỉnh 1 tháng/lần), nên so khách với **chính nó**:

```
avg_interval = TRUNG VỊ số ngày giữa 2 đơn liên tiếp, 12 tháng gần nhất
days_late    = (hôm nay − ngày đơn cuối) / avg_interval

days_late > 1.5  → 🟠 trễ nhịp
days_late > 2.5  → 🔴 nguy cơ mất khách
```

Dùng **trung vị** chứ không phải trung bình, để một đơn bất thường không làm lệch cả nhịp.

#### ② So kỳ trượt

```
L3M vs P3M  : 3 tháng gần nhất so 3 tháng trước đó → giảm > 30% = cảnh báo
QoQ, YoY    : so cùng kỳ để xử lý mùa vụ (Tết, mùa mưa — ngành pet có mùa rõ)
```

**Chỉ cảnh báo khi doanh số nền đủ lớn** (ngưỡng tiền trong `app_setting`) — nếu không, khách bé mua một thùng rồi nghỉ sẽ làm đầy danh sách bằng rác.

#### ③ Rụng SKU — tín hiệu sớm nhất

Khách rụng mặt hàng trước khi rụng doanh số.

```
basket_breadth = số SKU khác nhau khách mua trong kỳ
lost_line      = SKU khách mua ≥ 2 kỳ liên tiếp trước, kỳ này = 0
new_line       = ngược lại
```

Ghép thêm **MSL** (giai đoạn 2): `MSL compliance % = số SKU MSL khách đã mua / tổng SKU MSL của kênh đó`.

#### ④ ABC/Pareto — dùng để **xếp thứ tự ưu tiên**, không để cảnh báo

Khách hạng A (`customer.rank`, do công ty quy định) đang trễ nhịp → lên đầu "Việc hôm nay". Khách hạng C giảm mua → không đưa vào danh sách. Không có bước này thì danh sách cảnh báo dài 200 dòng và không ai đọc.

Kết quả của ①②③④ ghi vào `customer_health`, làm mới sau mỗi import.

### 9.3 Hiệu suất Sales và dự báo fail target

#### Cái bẫy phải tránh

Cách ngây thơ: *"đã trôi 50% tháng mà mới đạt 30% → sắp fail"*. **Cách này sai trong ngành phân phối**, vì đơn dồn về cuối kỳ: ngày 10 thì gần như *mọi* rep đều đang ở 30%, app sẽ báo đỏ toàn bộ và tính năng thành vô dụng.

#### Đường cong tiến độ

```
expected_pct = tỉ lệ doanh số LẼ RA đã đạt tại thời điểm này trong kỳ
             = phân bố thực tế của cùng kỳ năm trước   (khi đã có ≥ 1 năm dữ liệu)
             = đường thẳng theo ngày                    (khi chưa có — kèm nhãn "ước lượng thô")
             = hoặc tôi tự nhập (vd tuần 1/2/3/4 = 15/20/25/40%)
```

Lưu trong `period_pacing_curve`. Nguồn đường cong chọn được trong Cài đặt.

#### Pace index và xếp màu

```
pace_index = (đã đạt / chỉ tiêu) / expected_pct

  ≥ 1.00        🟢 đúng tiến độ
  0.90 – 1.00   🟡 chậm nhẹ
  0.75 – 0.90   🟠 chậm
  < 0.75        🔴 nguy cơ fail target
```

#### Ba con số để biết **làm gì**

```
projected_final = đã đạt / expected_pct               → "dự kiến về đích 82% chỉ tiêu"
gap             = chỉ tiêu − projected_final          → "thiếu 48 triệu"
required_rate   = (chỉ tiêu − đã đạt) / số ngày còn lại
current_rate    = đã đạt / số ngày đã qua
uplift_needed   = required_rate / current_rate − 1     → "cần tăng 35% nhịp bán/ngày"
```

#### Phiếu điểm rep — 8 chỉ số

Chỉ xem % đạt chỉ tiêu thì không biết rep sai ở đâu.

| # | Chỉ số | Nói lên điều gì |
|---|---|---|
| 1 | % đạt chỉ tiêu + `pace_index` (cả tiền và lượng) | Kết quả |
| 2 | **Độ phủ** = khách có mua trong kỳ / khách rep phụ trách | Rep chỉ chăm vài khách lớn hay đi hết tuyến |
| 3 | Khách **mới / mất / đang active** trong kỳ | Tăng trưởng thật hay ăn vào nền cũ |
| 4 | Giá trị đơn trung bình (AOV) | Chất lượng đơn |
| 5 | Số SKU trung bình mỗi đơn | Bán rộng hay chỉ bán hàng dễ |
| 6 | **MSL compliance** của khách thuộc rep *(giai đoạn 2)* | Độ kỷ luật khi đi thị trường |
| 7 | % nợ quá hạn / doanh số; số lần khách **hẹn rồi hoãn** | Bán được nhưng có đòi được tiền không |
| 8 | **% giá trị trả về / doanh số** | Dồn hàng ép chỉ tiêu cuối kỳ sẽ lộ ở đây |

Chỉ số 7 và 8 là hai cái file Excel rời không có — chúng chỉ xuất hiện khi ghép được ba nguồn dữ liệu.

Kết quả ghi vào `rep_performance`, làm mới sau mỗi import.

### 9.4 AI gợi ý khuyến mãi (giai đoạn 3)

Gọi Claude API từ app, **chỉ gửi số liệu đã tổng hợp** — không gửi tên khách, số điện thoại, hay giá vốn chi tiết. Việc tính tiền vẫn do code làm; AI chỉ đọc kết quả và đề xuất, tôi duyệt rồi kiểm lại bằng bộ mô phỏng. Cần mạng và API key (lưu trong cài đặt, **không** trong mã nguồn).

## 9.1 Import Excel: yêu cầu chi tiết

> Đây là **mục nhạy cảm**, mọi thay đổi luôn là task LARGE.

### Vấn đề gốc

File tải về theo **kỳ bất kỳ** — ngày, tuần, tháng, quý, nửa năm, năm — nên **các kỳ chồng lấn nhau**. Hệ quả: **không được** dùng kiểu "thấy `order_no` đã tồn tại thì bỏ qua", vì như vậy sẽ bỏ mất những dòng công ty đã sửa sau đó.

### Luồng

1. Chọn **loại file**: đơn hàng / hóa đơn / công nợ / **hàng trả về** / tồn kho.
2. Đọc bằng Apache POI (streaming reader cho file lớn), **ánh xạ cột** theo cấu hình trong `import_column_mapping` — lưu lại cho lần sau, không hardcode tên cột.
3. **Xem trước:** dòng hợp lệ / dòng lỗi (thiếu mã khách, ngày sai, số âm, rep không thuộc danh sách của tôi...) kèm lý do từng dòng.
4. Xác nhận thì ghi vào DB trong **một transaction**; lỗi giữa chừng → **hoàn tác toàn bộ**.
5. **Chống trùng bằng upsert, không phải bằng skip:**
   - Upsert theo khóa tự nhiên (`order_no` + số dòng, `invoice_no`, `customer.code`, `product.sku`)
   - Mỗi dòng lưu `row_hash` (hash các cột nghiệp vụ). Hash giống → bỏ qua. Hash khác → **update** và ghi vào lịch sử batch
   - Import lại cùng file: số liệu **không** nhân đôi, và dòng đã sửa **được cập nhật**
6. `import_batch` lưu loại file, tên file, **khoảng ngày bao phủ** (`period_from`, `period_to`), số dòng thêm / sửa / bỏ qua / lỗi, thời điểm. Re-import cùng kỳ → **thay thế** dữ liệu kỳ đó.
7. Mỗi batch **hủy được** (rollback cả lô sau khi đã commit).
8. Khách hoặc sản phẩm chưa có trong DB → **hỏi** tạo mới hay bỏ qua, không tự tạo im lặng.
9. Chạy ở thread nền, có thanh tiến trình và **hủy được**.

### Test bắt buộc

- Import lại cùng file → không nhân đôi
- Dòng đã sửa nội dung → được update, không bị bỏ qua
- Hai file có kỳ chồng lấn → không nhân đôi phần giao
- Lỗi ở dòng cuối → hoàn tác toàn bộ
- Hủy lô → số liệu trở về như trước
- Dòng có rep ngoài danh sách → bị bỏ qua kèm lý do
- Quy đổi thùng ↔ lẻ đúng khi file ghi lẫn hai đơn vị

## 10. Phi chức năng

- **Bảo mật:** mã hóa ổ đĩa (BitLocker) **bắt buộc**; mật khẩu DB trong `%APPDATA%/SalesManager/config.properties`, không trong mã nguồn; giá vốn và dữ liệu khách hàng không được log.
- **Backup:** `pg_dump` tự động khi đóng app, giữ N bản gần nhất, **thử khôi phục định kỳ**. Mất dữ liệu công nợ là rủi ro lớn nhất → backup làm ở M4, **trước** lần import dữ liệu thật đầu tiên.
- **Hiệu năng:** index theo `(customer_id, date)`; báo cáo và import chạy ở luồng nền (`Task`) để giao diện không đơ; bảng cache phân tích để "Việc hôm nay" mở tức thì.
- **Ngôn ngữ giao diện:** tiếng Việt, qua `messages_vi.properties` (không hardcode chữ). Tiền định dạng `1.234.567`, ngày `dd/MM/yyyy`.
- **Kiểm thử:** bắt buộc cho tuổi nợ (cả hai trục), doanh số theo kỳ (cả hai cơ sở ngày, có/không trừ trả về), `pace_index`, lệch nhịp mua hàng, rụng SKU, quy đổi đơn vị, và import (trùng, kỳ chồng lấn, lỗi, hoàn tác, hủy lô).
- **Quyền dữ liệu:** dữ liệu khách hàng, công nợ, giá vốn thuộc về công ty. Đã xác nhận được phép dùng trên máy cá nhân (ADR-20). File Excel thật để trong `samples/` — gitignored, và Claude bị chặn đọc. Khi đưa lên GitHub làm portfolio: **chỉ dùng dữ liệu giả**.

## 11. Việc còn phải chuẩn bị

| # | Việc | Chặn |
|---|---|---|
| 1 | **Mẫu 5 file Excel** (đơn hàng, hóa đơn, công nợ, hàng trả về, tồn kho) để trong `samples/`. Cần biết: tên cột, kiểu dữ liệu, định dạng ngày/số, cột nào là khóa | M5a |
| 2 | **Danh sách 5–10 sales rep** (mã + tên) và chỉ tiêu công ty giao cho từng rep | M1, M7 |
| 3 | **Danh sách kênh khách** thật và chính sách từng kênh (hạn mức, kỳ hạn, ngày nhắc, ngưỡng hạn dùng) — nhập trong app, không cần code | M3 |
| 4 | **Bảng giá theo kênh** (`price_tier`) | M3 |
| 5 | **Một ví dụ cụ thể cho mỗi dạng khuyến mãi** (mục 7) | **M12** |
| 6 | **Danh sách SKU bắt buộc (MSL)** theo từng kênh khách | M12 |
| 7 | Đường cong tiến độ kỳ: lấy từ lịch sử, hay tôi tự nhập tỉ lệ theo tuần? | M7b (dùng đường thẳng tạm nếu chưa có) |

## 12. Kế hoạch

Xem **`docs/ROADMAP.md`** — milestone M0–M19, Definition of Done riêng từng milestone, đường găng, sổ rủi ro, và phương án cắt ngắn để có bản dùng được sau ≈11 tuần.

Mỗi milestone kết thúc bằng **một thứ mở app lên dùng được và có test**, không để dồn đến cuối.
