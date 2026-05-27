<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# CozyCal

Ngắn gọn: CozyCal là một ứng dụng Android mẫu (template) chứa cấu trúc dự án, kết nối dữ liệu và giao diện cơ bản để phát triển tính năng đặt lịch.

## Nội dung README này
- **Mục tiêu:** Hướng dẫn nhanh cách cài đặt, cấu hình và chạy dự án trên môi trường phát triển.
- **Ngôn ngữ:** Tiếng Việt.

## Yêu cầu (Prerequisites)
- Java JDK 11+ (hoặc theo yêu cầu trong `compileOptions` của dự án)
- Android Studio (phiên bản tương thích với Android Gradle Plugin được dùng)
- Android SDK và các công cụ build tương ứng

## Cách cài đặt và chạy
1. Clone kho về máy:

```bash
git clone <your-repo-url>
cd cozycal
```

2. Mở project bằng Android Studio: chọn `Open` → trỏ tới thư mục dự án `cozycal`.
3. Android Studio có thể đề nghị cập nhật plugin hoặc cấu hình Gradle — cho phép nó thực hiện nếu cần.
4. Tạo (nếu cần) file cấu hình môi trường:

- Nếu dự án sử dụng biến môi trường, tham khảo file `.env.example` (nếu có) và tạo file `.env` ở thư mục gốc, nhớ **không** commit `.env` lên kho công khai.
- Nếu dùng Firebase, đảm bảo file cấu hình tương ứng đã được đặt (ví dụ `google-services.json` hoặc `firebase-applet-config.json`).

5. Chạy ứng dụng: chọn module `app` → Run trên thiết bị ảo (emulator) hoặc thiết bị thật.

## Cấu hình quan trọng
- `local.properties`: chứa đường dẫn SDK của Android; không commit vào Git.
- `gradle.properties`: các cấu hình build/feature flags.
- `firebase-applet-config.json`: (nếu dự án dùng) cấu hình Firebase.

## Build release
1. Chuẩn bị keystore và thêm thông tin signing vào `app/build.gradle.kts` hoặc `signingConfigs`.
2. Chạy Gradle task tạo bản phát hành (`assembleRelease`) hoặc dùng Android Studio → Build → Generate Signed Bundle / APK.

## Gợi ý debug và phát triển
- Sử dụng Logcat để xem log runtime.
- Viết unit test trong `app/src/test` và instrumentation test trong `app/src/androidTest`.

## Đóng góp
- Mở issue cho bug hoặc yêu cầu tính năng.
- Tạo pull request có mô tả rõ thay đổi và cách kiểm thử.