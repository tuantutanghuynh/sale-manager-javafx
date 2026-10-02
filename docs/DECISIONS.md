# DECISIONS — Architecture Decision Records

Mới nhất ở dưới cùng. Khi nào phải ghi ADR: `CLAUDE.md` mục 12.

---

## ADR-1: Dùng PostgreSQL 16 thay vì SQLite — 2026-10-02
**Bối cảnh:** app một người dùng, một máy. SQLite chỉ là một file, không cần cài gì, backup chỉ cần chép file.
**Quyết định:** PostgreSQL 16 chạy trên máy.
**Lý do:** (1) Báo cáo doanh số, tuổi nợ, pace index là truy vấn tổng hợp nặng — Postgres có `DATE_TRUNC`, window function, `NUMERIC` đúng nghĩa; SQLite kiểu dữ liệu tiền/ngày lỏng hơn. (2) Sau này muốn đưa DB lên VPS để xem từ máy khác thì không phải viết lại.
**Phương án đã loại:** SQLite — đơn giản hơn nhiều cho một người dùng, nhưng phải viết lại toàn bộ SQL tổng hợp nếu sau này đổi.
**Hệ quả:** phải cài Postgres trên máy; backup dùng `pg_dump` chứ không phải chép file; `pg_dump`/`pg_restore` phải có trong `PATH` hoặc khai đường dẫn trong `config.properties`. Rollback: đổi sang SQLite cần viết lại tầng repository + toàn bộ migration.

## ADR-2: `CLAUDE.md` thắng house style Java cá nhân — 2026-10-02
**Bối cảnh:** house style cá nhân (singleton tĩnh `getInstance()`, `new Thread` tự tay, `printStackTrace` + `return false` trong repository, `System.out.printf` làm log, không bao giờ Javadoc, Java 17) chống nhau trực tiếp với `CLAUDE.md` ở 6 điểm.
**Quyết định:** theo `CLAUDE.md`: constructor DI, `Task` + executor dùng chung, SLF4J + Logback, Javadoc ngắn cho method public của service, Java 21.
**Lý do:** (1) App dùng thật trong công việc, dữ liệu công nợ mất là mất thật — `printStackTrace` rồi `return false` làm lỗi import biến mất không dấu vết. (2) Spec yêu cầu test service mà không mở UI; singleton tĩnh làm việc đó khó hơn nhiều.
**Phương án đã loại:** giữ house style — ngắn hơn khi viết, nhưng đánh đổi bằng khả năng chẩn đoán lỗi và khả năng test.
**Hệ quả:** giữ lại từ house style những thứ không xung đột: khối comment `//` ở đầu mỗi class nói **vì sao** class tồn tại, `Validator` fail-fast với `requireXxx`, message lỗi dạng `"<Field> must ..."`, row mapper luôn tên `mapRow`.

## ADR-3: Package `com.tuantu.salesapp`; UI nhóm theo tính năng — 2026-10-02
**Bối cảnh:** spec v2 nhóm UI theo tính năng (`ui/customers/`), bản mẫu `CLAUDE.md` nhóm theo loại (`controller/`, `view/`).
**Quyết định:** UI nhóm **theo tính năng**; các tầng còn lại (`service`, `repository`, `domain`, ...) nhóm **theo loại**.
**Lý do:** app có 12 màn hình. Nhóm theo loại thì sửa một màn hình phải nhảy giữa 3 thư mục cách xa nhau. Các tầng còn lại thì ngược lại — gom theo loại dễ thấy pattern chung.
**Phương án đã loại:** nhóm toàn bộ theo loại; nhóm toàn bộ theo tính năng (service dùng chung sẽ không biết đặt đâu).
**Hệ quả:** FXML đặt trong `resources` cùng package path với controller.

## ADR-4: Không dùng JPMS, chạy trên classpath — 2026-10-02
**Bối cảnh:** JavaFX khuyến khích JPMS (`module-info.java`), nhưng JPMS gây `InaccessibleObjectException` khi FXML/TableView dùng reflection và phải khai `opens` cho từng package.
**Quyết định:** không dùng `module-info.java`; chạy trên classpath; `jpackage` dùng runtime từ `jlink` khai module thủ công.
**Lý do:** app một người, không phát hành thư viện, không cần đóng gói kín module. Bỏ JPMS loại bỏ một nhóm lỗi khó hiểu mà không mất gì.
**Phương án đã loại:** JPMS — đúng chuẩn hơn, nhưng thêm chi phí học và chi phí sửa lỗi cho người đang học JavaFX.
**Hệ quả:** `Launcher.java` phải tách khỏi `App.java` để tránh "JavaFX runtime components are missing" khi chạy từ jar. Thêm JPMS về sau là task LARGE.

## ADR-5: Chỉ hỗ trợ Windows — 2026-10-02
**Bối cảnh:** `jpackage` phải chạy trên đúng OS đích.
**Quyết định:** chỉ Windows 10/11.
**Lý do:** chỉ dùng trên một máy Windows. Thêm macOS/Linux = thêm máy build, thêm icon, thêm vòng smoke test.
**Hệ quả:** `AppPaths` chỉ cần nhánh `%APPDATA%`, nhưng vẫn viết sao cho thêm nhánh khác về sau không phải sửa chỗ gọi.

## ADR-6: Chế độ hướng dẫn giới hạn ở Java / FXML / CSS — 2026-10-02
**Bối cảnh:** mục đích của dự án là vừa làm vừa học Java/JavaFX. Bản mẫu `CLAUDE.md` cấm Claude sửa **mọi** file, kể cả `.gitignore` và `docs/`.
**Quyết định:** Claude **không** sửa `.java`, `.fxml`, `.css` (chỉ hướng dẫn trong chat); Claude **được** tự viết `docs/`, `.claude/`, `.gitignore`, migration SQL, `i18n/*.properties`, `application.properties`. `pom.xml` phải trình plan trước.
**Lý do:** học Java và JavaFX là mục tiêu; học cú pháp `.gitignore` thì không. Dành thời gian cho phần đáng học.
**Phương án đã loại:** cấm tuyệt đối (chậm, không thêm giá trị học); cho Claude viết hết (mất mục tiêu học).
**Hệ quả:** chặn cứng bằng `permissions.deny` trong `.claude/settings.json`. Đây là lớp chặn bổ sung chứ không tuyệt đối — Claude vẫn ghi file gián tiếp qua shell được, nên vẫn phải review `git diff`.

## ADR-7: Viết FXML tay; không dựng app bằng WebView — 2026-10-02
**Bối cảnh:** câu hỏi "có cách nào đẹp hơn Scene Builder hay viết tay không".
**Quyết định:** viết FXML tay, Scene Builder chỉ mở để xem trước. Giao diện đẹp đến từ AtlantaFX + Ikonli + `base.css`. WebView chỉ xét ở M16 cho đúng màn hình dashboard.
**Lý do:** Scene Builder và viết tay sinh ra **cùng một file FXML và cùng một giao diện** — công cụ soạn không quyết định độ đẹp. Scene Builder sinh nhiều `prefWidth` cứng (phá responsive) và hay ghi đè phần sửa tay.
**Phương án đã loại:** WebView cho cả app (WebKit cũ, gõ chữ lệch giao diện, phải bảo trì cầu nối Java↔JS cho mọi màn hình); Compose Multiplatform / Electron / Tauri (đổi hẳn ngôn ngữ, bỏ toàn bộ spec).
**Hệ quả:** phải chốt một nền tảng thiết kế ở M2 (thang 8px, một thang cỡ chữ, một bảng màu, 3 màu trạng thái) và mọi màn hình sau dùng lại.

## ADR-8: Test DB là `salesmanager_test`, không dùng Testcontainers — 2026-10-02
**Bối cảnh:** spec v2 đề xuất Testcontainers để test trên Postgres thật. Testcontainers cần Docker Desktop.
**Quyết định:** một database riêng `salesmanager_test` trong Postgres local, Flyway chạy migration thật, clean trước mỗi lần chạy.
**Lý do:** nhanh hơn nhiều, không cần cài Docker trên máy cá nhân.
**Phương án đã loại:** Testcontainers — môi trường test sạch hơn, nhưng chi phí cài và thời gian khởi động không đáng cho một dự án một người.
**Hệ quả:** **rủi ro test chạy vào DB thật.** Bắt buộc có **guard** trong code test: tên database không kết thúc bằng `_test` → fail ngay, không chạy tiếp. Thêm `psql`, `dropdb`, `mvn flyway:clean` vào `permissions.deny`.

## ADR-9: Dùng `ResourceBundle` dù chỉ có một ngôn ngữ — 2026-10-02
**Bối cảnh:** app chỉ dùng tiếng Việt, một người dùng. Hardcode chữ sẽ nhanh hơn.
**Quyết định:** mọi chuỗi hiển thị nằm trong `i18n/messages_vi.properties`; FXML dùng `%key`, code dùng `bundle.getString(...)`.
**Lý do:** giữ đúng rule "không hardcode chuỗi hiển thị" ở `CLAUDE.md` mục 5. Lợi ích thực tế: sửa chữ ở một nơi, và thấy ngay toàn bộ chữ trong app ở một file.
**Phương án đã loại:** hardcode tiếng Việt — nhanh hơn khi viết, nhưng chữ rải khắp FXML và code, sửa thuật ngữ nghiệp vụ phải tìm nhiều chỗ.
**Hệ quả:** mỗi màn hình mới phải thêm key vào `messages_vi.properties`; `/check-fx` kiểm tra mục này.

## ADR-10: Spotless + palantir-java-format — 2026-10-02
**Bối cảnh:** cần một chuẩn format tự động. `google-java-format` dùng 2 space, trái với rule 4 space ở `CLAUDE.md` mục 5.
**Quyết định:** Spotless + `palantir-java-format` (4 space). `mvn spotless:check` nằm trong Definition of Done.
**Lý do:** hết tranh luận format; giữ được 4 space.
**Phương án đã loại:** google-java-format (sai indent); không format tự động (diff bẩn vì khác biệt khoảng trắng).

## ADR-11: Khách hàng có hai trục phân loại độc lập — 2026-10-02
**Bối cảnh:** spec v2 chỉ có một khái niệm "nhóm khách", nhưng thực tế có hai thứ khác nhau: kênh bán (khách tỉnh / thành phố / đại lý / sỉ / hotel / MT / thương mại điện tử) quyết định **bảng giá**, và hạng A/B/C theo doanh số quyết định **ưu tiên**.
**Quyết định:** `customer.group_id` → `customer_group` = **kênh bán** (gắn `price_tier` + chính sách công nợ). `customer.rank` = **A/B/C**, **do công ty quy định** (nhập tay hoặc lấy từ file import).
**Lý do:** một khách hotel có thể hạng A hoặc C — hai trục độc lập. Gộp vào một cột thì không diễn đạt được.
**Phương án đã loại:** một cột duy nhất; app tự tính hạng (loại vì hạng chính thức là của công ty — app chỉ có thể **đề xuất** để đối chiếu).
**Hệ quả:** mọi báo cáo và bộ lọc phải có cả hai chiều. `customer_health` dùng `rank` để xếp ưu tiên (spec 9.2 ④).

## ADR-12: Địa giới 2 cấp, chọn từ bảng danh mục — 2026-10-02
**Bối cảnh:** cần lọc và báo cáo theo khu vực.
**Quyết định:** `customer.province_id` + `customer.ward_id` (tỉnh/thành + phường/xã, **không có cấp huyện**); hai bảng danh mục `province`, `ward` được seed; giao diện dùng **dropdown**, không cho nhập chữ tự do.
**Lý do:** nhập tự do thì "TP.HCM" / "Hồ Chí Minh" / "HCM" thành ba khu vực khác nhau và báo cáo theo tỉnh sẽ sai.
**Phương án đã loại:** cột text tự do (nhanh hơn, nhưng dữ liệu bẩn không sửa được về sau).
**Hệ quả:** cần seed danh sách tỉnh/phường trong `V1__init.sql`. Danh mục hành chính đổi thì cần migration thêm bản ghi, không đổi bản ghi cũ (khách cũ phải giữ được khu vực lịch sử).

## ADR-13: Đơn vị đóng gói, lưu quy cách tại thời điểm bán — 2026-10-02
**Bối cảnh:** spec v2 thiếu hoàn toàn phần đơn vị. Thực tế sản phẩm bán theo **thùng và lẻ**, và quy cách thùng **đổi theo thời gian**.
**Quyết định:** `product(base_uom, pack_uom, units_per_pack)`. `order_item(qty, uom, units_per_pack_at_sale, qty_base)`. Mọi phép cộng số lượng và mọi chỉ tiêu theo lượng dùng `qty_base`.
**Lý do:** không có `qty_base` thì không cộng được "3 thùng" với "12 lẻ". Không lưu `units_per_pack_at_sale` thì đổi quy cách thùng sẽ làm sai số liệu mọi đơn cũ — cùng lý do spec lưu `unit_cost` tại thời điểm bán.
**Phương án đã loại:** một đơn vị duy nhất (không khớp thực tế); chỉ lưu quy cách hiện tại trong `product` (số liệu lịch sử sẽ sai sau khi đổi quy cách).
**Hệ quả:** importer phải quy đổi khi đọc file; cần test riêng cho file ghi lẫn hai đơn vị.

## ADR-14: Hai quan hệ sales rep khác nhau — 2026-10-02
**Bối cảnh:** tôi phụ trách 5–10 rep. Spec v2 chỉ lưu `sales_rep_name` dạng chữ trong đơn hàng.
**Quyết định:** bảng `sales_rep(code, name, is_active, joined_at, left_at)`; **hai** quan hệ: `customer.sales_rep_id` (ai phụ trách khách) và `sales_order.sales_rep_id` (ai viết đơn).
**Lý do:** khách của rep A nhưng rep B viết thay là chuyện thường. Độ phủ và chất lượng công nợ tính theo rep **phụ trách**; doanh số tính theo rep **viết đơn**. Một cột không diễn đạt được cả hai.
**Phương án đã loại:** giữ `sales_rep_name` dạng chữ (không join được, rep đổi tên là vỡ); một quan hệ duy nhất.
**Hệ quả:** rep nghỉ việc **không xoá**, chỉ `is_active = false`, để báo cáo kỳ cũ vẫn đúng. Import chỉ nhận dòng có rep trong danh sách của tôi.

## ADR-15: Import dùng upsert + `row_hash` + batch theo kỳ, không dùng "skip nếu đã tồn tại" — 2026-10-02
**Bối cảnh:** file Excel tải về theo kỳ bất kỳ (ngày/tuần/tháng/quý/nửa năm/năm) nên **các kỳ chồng lấn nhau**.
**Quyết định:** upsert theo khóa tự nhiên; mỗi dòng lưu `row_hash` (hash giống → bỏ qua, khác → update); `import_batch` lưu `period_from`/`period_to`; re-import cùng kỳ thì **thay thế** dữ liệu kỳ đó; mỗi batch hủy được.
**Lý do:** "thấy `order_no` đã tồn tại thì bỏ qua" sẽ **bỏ mất những dòng công ty đã sửa** sau lần import trước. Đây là lỗi im lặng, không ai phát hiện cho tới khi báo cáo lệch.
**Phương án đã loại:** skip-if-exists (đơn giản hơn nhiều, nhưng sai); xoá hết rồi import lại (mất dữ liệu nhập tay như `promised_date`).
**Hệ quả:** M5a thành milestone riêng, dài hơn, và là **mục nhạy cảm** — mọi thay đổi về sau là task LARGE. Phải có bộ 7 test ở spec mục 9.1 pass trước khi sang M5b. Rollback: khôi phục từ `backup/` và import lại.

## ADR-16: Công nợ hai trục: hạn hợp đồng và ngày khách hẹn — 2026-10-02
**Bối cảnh:** trong ngành pet, công ty quy định 10 hoặc 30 ngày, nhưng thực tế đòi được tiền rất khác.
**Quyết định:** `receivable` có cả `due_date` (hợp đồng) và `promised_date` (khách hẹn); bảng `collection_followup` lưu lịch sử hẹn (hẹn lần thứ mấy, ngày hẹn, có giữ lời không). Tuổi nợ báo theo **cả hai trục**.
**Lý do:** tuổi nợ theo `due_date` cho biết đúng/sai hợp đồng; theo `promised_date` cho biết **hôm nay cần gọi ai**. Khách hẹn rồi hoãn 3 lần là tín hiệu khác hoàn toàn với khách hẹn một lần rồi trả.
**Phương án đã loại:** chỉ `due_date` (không phản ánh thực tế); ghi ngày hẹn vào trường ghi chú dạng chữ (không truy vấn được).
**Hệ quả:** `promised_date` là **dữ liệu nhập tay**, không đến từ file import → importer không được ghi đè nó. Đây là lý do không dùng "xoá hết rồi import lại" ở ADR-15.

## ADR-17: Một bảng `kpi_target` cho mọi tổ hợp, `NULL` = "tất cả" — 2026-10-02
**Bối cảnh:** chỉ tiêu có 3 chiều đối tượng (rep / khách / sản phẩm), 2 thước đo (tiền / lượng), 3 loại kỳ (tháng / quý / năm). Công ty giao cả tổ hợp (rep X, SKU Y, tháng 10).
**Quyết định:** một bảng với `rep_id`, `customer_id`, `product_id` cho phép `NULL` (nghĩa là "tất cả"), cộng `metric`, `period_type`, `period_start`, `target_value`, UNIQUE trên toàn bộ tổ hợp.
**Lý do:** làm mỗi loại một bảng sẽ thành 6 bảng với cùng logic tính tiến độ. Một bảng diễn đạt được mọi tổ hợp, kể cả tổ hợp chưa nghĩ tới.
**Phương án đã loại:** `kpi_target` + `customer_target` riêng như spec v2 (không diễn đạt được chỉ tiêu theo rep hay theo SKU); bảng `scope_type`/`scope_id` dạng polymorphic (không ràng buộc được FK, không diễn đạt được tổ hợp 2 chiều).
**Hệ quả:** cần **quy tắc đo kết quả**: khi có cả chỉ tiêu tổng và chi tiết, đo theo **chỉ tiêu cụ thể nhất có mặt**; **không cộng dồn** chi tiết lên tổng, vì công ty thường giao tổng ≠ tổng các phần. Quy tắc này phải có test.

## ADR-18: Dự báo fail target dùng đường cong tiến độ lịch sử, không dùng đường thẳng — 2026-10-02
**Bối cảnh:** cần cảnh báo rep nào sắp không đạt chỉ tiêu.
**Quyết định:** `expected_pct` lấy từ phân bố doanh số thực tế của cùng kỳ năm trước (`period_pacing_curve`); chưa có ≥ 1 năm dữ liệu thì dùng đường thẳng **kèm nhãn "ước lượng thô"**, hoặc tôi tự nhập tỉ lệ theo tuần. `pace_index = (đã đạt / chỉ tiêu) / expected_pct`, phân 4 mức màu. Kèm `projected_final`, `gap`, `uplift_needed`.
**Lý do:** trong ngành phân phối, đơn dồn về cuối kỳ. So với đường thẳng thì ngày 10 gần như **mọi** rep đều đang ở ~30% và app sẽ báo đỏ toàn bộ → tính năng thành vô dụng và bị tắt.
**Phương án đã loại:** run-rate tuyến tính (sai vì bỏ qua dồn cuối kỳ); chỉ hiện % đạt mà không dự báo (không cảnh báo được sớm).
**Hệ quả:** cần ≥ 1 năm dữ liệu lịch sử để chính xác → nên import 12 tháng quá khứ ở M5b–M6. Mọi ngưỡng (1.00 / 0.90 / 0.75) nằm trong `app_setting`. Công thức là **mục nhạy cảm**, sửa là task LARGE.

## ADR-19: `customer_health` / `rep_performance` là bảng cache, không tính tại chỗ — 2026-10-02
**Bối cảnh:** "Việc hôm nay" là trang mở đầu. Tính lệch nhịp, rụng SKU, pace index cho toàn bộ khách và rep là truy vấn nặng.
**Quyết định:** ba bảng cache `customer_health`, `rep_performance`, `period_pacing_curve`, làm mới sau mỗi lần import.
**Lý do:** tính tại chỗ thì mở app phải chờ. Dữ liệu trong ba bảng này **luôn dựng lại được từ dữ liệu gốc**, nên mất không sao.
**Phương án đã loại:** tính on-the-fly (chậm); materialized view của Postgres (ít kiểm soát thời điểm refresh, khó test bằng JUnit).
**Hệ quả:** phải có hàm "tính lại toàn bộ" gọi được từ Cài đặt, để sửa lỗi công thức không cần import lại. Backup không cần ba bảng này.

## ADR-20: Dùng dữ liệu thật của công ty trên máy cá nhân — 2026-10-02
**Bối cảnh:** dữ liệu khách hàng, công nợ, giá vốn thuộc về công ty. App chạy trên máy cá nhân.
**Quyết định:** đã xác nhận được phép. File Excel thật để nguyên, **không cần xóa thông tin nhạy cảm**, đặt trong `samples/`.
**Lý do:** app là công cụ làm việc thật; dữ liệu đã xóa thông tin thì không kiểm chứng được logic nghiệp vụ.
**Hệ quả — ba lớp bảo vệ bắt buộc:**
1. **Mã hóa ổ đĩa (BitLocker)** — đăng nhập cục bộ không bảo vệ file DB.
2. `.gitignore` chặn `samples/`, `*.xlsx`, `backup/`, `*.dump`, `config.properties`. Lên GitHub làm portfolio: **chỉ dữ liệu giả**.
3. `permissions.deny` chặn Claude đọc `samples/` và `backup/`; **không dán nội dung file Excel thật vào chat** — chỉ dán dòng tiêu đề + 2–3 dòng đã thay tên và số.

Ngoài ra: không log tên khách, số điện thoại, số nợ cụ thể, giá vốn. Giai đoạn 3 gọi Claude API chỉ gửi số liệu đã tổng hợp.

## ADR-21: Doanh số tính sau VAT, cơ sở ngày đổi được — 2026-10-02
**Bối cảnh:** spec v2 để hở câu "doanh số theo ngày đơn hay ngày hóa đơn, có trừ hàng trả về không".
**Quyết định:** lưu đủ `vat_rate`, `amount_excl_vat`, `vat_amount`, `amount_incl_vat` trong `order_item`. Ba tham số nằm trong `app_setting`: cơ sở ngày (`ORDER_DATE` | `INVOICE_DATE`), trừ hàng trả về (có/không), trước/sau VAT. Mặc định: `INVOICE_DATE`, **có** trừ trả về, **sau VAT**.
**Lý do:** yêu cầu là "cover đủ các trường hợp để app linh hoạt". Lưu cả ba con số VAT thì sau này cần so trước VAT vẫn có, không phải import lại.
**Phương án đã loại:** chỉ lưu một con số tổng (không đổi được cách tính về sau).
**Hệ quả:** mọi test doanh số phải chạy cho **cả hai** cơ sở ngày và **cả hai** trạng thái trừ/không trừ trả về. Hàng trả về là loại file import riêng.

## ADR-22: Không dùng ViewModel ở M2–M6 — 2026-10-02
**Bối cảnh:** bản mẫu `CLAUDE.md` để ViewModel là tuỳ chọn.
**Quyết định:** các màn hình đầu (M2–M6) để controller gọi service trực tiếp, không có tầng ViewModel. Màn hình nào state phức tạp (nhiều trường phụ thuộc nhau, validate thời gian thực) thì thêm ViewModel **cho riêng màn hình đó** và ghi ADR mới.
**Lý do:** đang học JavaFX; thêm một tầng ngay từ màn hình đầu làm khó hiểu luồng dữ liệu mà chưa giải quyết vấn đề thật nào.
**Phương án đã loại:** ViewModel từ đầu cho mọi màn hình.
**Hệ quả:** màn hình Import (M5a) và Hiệu suất Sales (M7b) có nhiều state nhất — khả năng cao sẽ cần ViewModel, sẽ quyết định khi tới đó.

## ADR-23: RFM hoãn sang giai đoạn 3 — 2026-10-02
**Bối cảnh:** cần mô hình phát hiện "khách giảm mua". RFM (Recency–Frequency–Monetary) là mô hình phổ biến nhất.
**Quyết định:** giai đoạn 1 làm **lệch nhịp mua hàng** + **rụng SKU** + xếp ưu tiên ABC. RFM hoãn sang M18.
**Lý do:** RFM nhìn đẹp trên dashboard nhưng chỉ nói "khách này đang tệ", không nói **làm gì**. Lệch nhịp nói "khách này đã quá nhịp 2.3 lần, gọi ngay"; rụng SKU nói "khách mất 4 mặt hàng quý này, mang mẫu đi". Hai cái sau actionable hơn nhiều cho công việc đi thị trường.
**Phương án đã loại:** làm RFM trước (dễ hiểu hơn, nhưng không dùng được ngay).
**Hệ quả:** không có bảng segment ở giai đoạn 1. Thêm RFM ở M18 chỉ cần đọc dữ liệu có sẵn, không cần migration.

## ADR-24: `jlink` chỉ gói runtime JDK+JavaFX; app chạy trên classpath, không fat jar — 2026-10-02
**Bối cảnh:** ADR-4 chọn không dùng JPMS, nhưng `CLAUDE.md` mục 14 (bản mẫu) ghi "dùng `jlink` tạo runtime tối thiểu rồi `jpackage`". Hai thứ chống nhau: `jlink` chỉ gói được **module**, nên không gói được chính app khi app không phải module.
**Quyết định:**
1. `jlink` tạo runtime chỉ gồm module của **JDK + JavaFX** (`javafx.controls`, `javafx.fxml`, `java.sql`, `java.net.http`, `jdk.crypto.ec`).
2. App đóng gói thành **jar thường + thư mục `lib/`**, chạy trên classpath. **Không dùng fat jar** (`maven-shade-plugin`).
3. `jpackage --runtime-image <runtime> --input target/app --main-jar <app>.jar` tạo `.msi`.
**Lý do:** fat jar trộn native library của JavaFX (`.dll` theo nền tảng) vào một jar hay gây lỗi khó lần khi tải native lib. Jar + `lib/` giữ nguyên cấu trúc mà `jpackage` mong đợi, và vẫn gọn vì runtime đã được `jlink` cắt nhỏ.
**Phương án đã loại:** chuyển sang JPMS để `jlink` gói được cả app (mâu thuẫn ADR-4); fat jar bằng shade plugin (rủi ro native lib); `jpackage` không kèm `--runtime-image` (gói cả JRE đầy đủ, bộ cài phình gấp nhiều lần).
**Hệ quả:**
- `pom.xml` cần `maven-jar-plugin` ghi `Class-Path` vào manifest với prefix `lib/`, và `maven-dependency-plugin` copy dependency sang `target/app/lib/`. Output gom về `target/app/` để `jpackage --input` trỏ vào một chỗ.
- **Lệnh `jpackage` đầy đủ chưa được kiểm chứng** — đó là việc của **spike S1** sau M2. Nếu S1 cho thấy cách này không chạy, phương án dự phòng là `jpackage` không kèm `--runtime-image` (bộ cài to hơn nhưng chắc chắn chạy).
- `CLAUDE.md` mục 14 đã được sửa cho khớp quyết định này.
