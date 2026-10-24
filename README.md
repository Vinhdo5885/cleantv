# CleanTV Launcher (Bản hỗ trợ Android 4.4 KitKat - API 19+)

Giao diện TV siêu nhẹ, tối giản phong cách **Apple TV / CleanYourTV**, được tùy biến đặc biệt cho các dòng SmartBox cấu hình thấp và chạy Android cũ như **VNPT SmartBox 2** (chip Amlogic S805, 1GB RAM).

---

## ✨ Điểm nổi bật:
* **Tương thích Android 4.4 (KitKat - `minSdkVersion 19`)**: Khắc phục hoàn toàn lỗi `INSTALL_FAILED_OLDER_SDK` của CleanYourTV gốc trên GitHub (vốn yêu cầu Android 8.0+).
* **Phong cách Apple TV / CleanYourTV**:
  * Nền tối tối giản (Dark OLED), tiết kiệm tài nguyên.
  * Hiệu ứng thẻ mượt mà: Tự động phóng to (Scale 1.1x) và viền sáng khi remote di chuyển đến.
  * Đồng hồ số lớn và hiển thị ngày tháng tiếng Việt.
* **Thanh Dock Yêu thích (Favorites)**: Cho phép ghim các app hay dùng (YouTube, VTV Go, Trình duyệt...) lên hàng đầu.
* **Dọn dẹp app rác VNPT không cần root**:
  * Nhấn giữ phím **OK** (hoặc bấm phím **MENU**) trên remote vào bất kỳ app nào để:
    * *Ghim / Bỏ ghim Yêu thích*.
    * *Ẩn app rác khỏi màn hình* (xóa bỏ giao diện lộn xộn của nhà mạng).
    * *Xem thông tin / Gỡ cài đặt*.
* **Siêu nhẹ & Tiết kiệm RAM**:
  * Không dịch vụ chạy ngầm, không quảng cáo, không gửi dữ liệu.
  * Chiếm chưa đến **15MB RAM** khi hoạt động, giúp box VNPT chạy mượt mà không bị treo giật.

---

## 🛠 Cách biên dịch & Xuất file APK:

### Cách 1: Sử dụng Android Studio (Dễ nhất)
1. Mở **Android Studio**.
2. Chọn **File -> Open...** và trỏ đến thư mục `cleantv`.
3. Chờ Gradle đồng bộ (Sync) xong.
4. Trên thanh menu, chọn: **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
5. Sau khi build hoàn tất, Android Studio sẽ hiện thông báo kèm nút **locate** trỏ đến file `app-debug.apk`.

### Cách 2: Dùng dòng lệnh (Terminal / Command Prompt)
Tại thư mục `cleantv`, chạy lệnh:
```powershell
.\gradlew assembleDebug
```
File APK xuất ra sẽ nằm tại:
`app\build\outputs\apk\debug\app-debug.apk`

---

## 📺 Cách cài đặt lên VNPT SmartBox 2:
1. Chép file `app-debug.apk` vừa tạo vào USB.
2. Cắm USB vào cổng USB của VNPT SmartBox 2.
3. Mở ứng dụng **Quản lý file / File Manager** trên box -> Chọn file APK và bấm **Cài đặt**.
4. Bấm phím **Home** trên remote -> Chọn **CleanTV** -> Chọn **Luôn luôn (Always)**.
