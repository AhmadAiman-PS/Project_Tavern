# Tavern App - Fitur Terbaru

## 📝 Ringkasan Perubahan

Aplikasi Tavern telah diupdate dengan fitur-fitur baru:
1. **Halaman Profil Pengguna**
2. **Sistem Pencarian Postingan**
3. **Navigasi ke Diskusi dari Histori Postingan**
4. **Postingan Multi-User dengan Komentar Lintas Akun**

---

## ✨ Fitur 1: Halaman Profil Pengguna

### Requirement
Halaman profil yang menampilkan:
- ✅ Foto Avatar
- ✅ Bio singkat
- ✅ List History Postingan User
- ✅ Tanggal bergabung (Cake day)

### Acceptance Criteria
- ✅ Informasi profil tampil sesuai dengan user yang sedang login/dilihat
- ✅ User bisa mengupdate Bio dan Avatar
- ✅ Tab "My Posts" menampilkan daftar postingan yang pernah dibuat user tersebut

### Cara Menggunakan
1. Klik icon **Person** (profil) di bagian kiri atas halaman feed
2. Akan menampilkan profil Anda dengan:
   - Avatar (inisial nama)
   - Username
   - Tanggal bergabung
   - Bio
   - Daftar semua postingan yang telah dibuat
3. Klik icon **Edit** untuk mengubah Bio dan Avatar
4. Klik **Save** untuk menyimpan perubahan

### Implementasi Teknis
**File Baru:**
- `ProfileScreen.kt` - UI halaman profil

**File yang Diupdate:**
- `UserEntity.kt` - Menambahkan field `bio`, `avatarUrl`, `joinedDate`
- `UserDao.kt` - Menambahkan fungsi `getUserByUsername`, `getUserByUsernameFlow`, `updateUser`
- `PostDao.kt` - Menambahkan fungsi `getPostsByAuthor`
- `TavernRepository.kt` - Menambahkan fungsi profile management
- `TavernViewModel.kt` - Menambahkan state dan fungsi profile
- `TavernApp.kt` - Menambahkan navigasi ke ProfileScreen

---

## 🔍 Fitur 2: Sistem Pencarian Postingan

### Requirement
Mekanisme search engine internal dengan:
- ✅ Query Input: Kata kunci yang diketik user
- ✅ Scope Pencarian: Posts (dapat diperluas ke Communities/Users)
- ✅ Parameter Filter: Pencarian real-time
- ✅ Parameter Sorting: Hasil terbaru terlebih dahulu

### Acceptance Criteria
- ✅ Bar pencarian dapat diakses dari header di setiap halaman feed
- ✅ Sistem menampilkan daftar hasil yang mengandung kata kunci (pada Judul atau Isi Post)
- ✅ Hasil pencarian dapat diklik dan mengarahkan user ke Detail Post
- ✅ Jika data tidak ditemukan, muncul tampilan "No posts found"

### Cara Menggunakan
1. Di halaman feed, klik icon **Search** di bagian kanan atas
2. Ketik kata kunci pencarian
3. Hasil pencarian akan muncul secara real-time
4. Klik icon **X** untuk menutup pencarian dan kembali ke feed utama
5. Klik salah satu hasil untuk membuka detail postingan

### Implementasi Teknis
**File yang Diupdate:**
- `PostDao.kt` - Menambahkan query `searchPosts` dengan LIKE operator
- `TavernRepository.kt` - Menambahkan fungsi `searchPosts`
- `TavernViewModel.kt` - Menambahkan:
  - State: `searchQuery`, `searchResults`, `isSearching`
  - Fungsi: `updateSearchQuery`, `clearSearch`
- `TavernApp.kt` - Menambahkan:
  - Search bar di TopAppBar
  - Search indicator banner
  - Conditional rendering (search results vs all posts)

---

## 💬 Fitur 3: Navigasi ke Diskusi dari Histori

### Requirement
Histori postingan ketika diklik akan menuju ke diskusi postingannya

### Acceptance Criteria
- ✅ Setiap postingan di halaman profil dapat diklik
- ✅ Klik postingan akan membuka halaman diskusi dengan komentar

### Cara Menggunakan
1. Buka halaman profil (klik icon Person)
2. Di bagian "My Posts", klik salah satu postingan
3. Akan membuka halaman diskusi dengan semua komentar

### Implementasi Teknis
**File yang Diupdate:**
- `ProfileScreen.kt` - PostCard dengan onClick handler ke `viewModel.selectPost(post)`
- Menggunakan navigasi yang sudah ada di `TavernApp.kt`

---

## 🌐 Fitur 4: Postingan Multi-User

### Requirement
Postingan dari akun berbeda dapat muncul pada halaman utama, sehingga akun lain dapat berkomentar dalam postingan user lain

### Acceptance Criteria
- ✅ Semua postingan dari semua user muncul di feed
- ✅ User dapat mengklik nama author untuk melihat profil mereka
- ✅ User dapat berkomentar di postingan user lain
- ✅ Komentar menampilkan nama author yang berbeda

### Cara Menggunakan
1. Di halaman feed, semua postingan dari semua user akan tampil
2. Klik postingan untuk membuka diskusi
3. Tambahkan komentar di bagian bawah
4. Komentar Anda akan muncul dengan username Anda
5. Klik nama author (badge biru) pada postingan untuk melihat profil mereka

### Implementasi Teknis
**Sudah Ada Sejak Awal:**
- `PostDao.getAllPosts()` - Mengambil semua postingan tanpa filter
- `CommentEntity` - Menyimpan author untuk setiap komentar
- Comment system yang sudah support multi-user

**Improvement Baru:**
- Clickable author badge pada PostCard yang membuka profil
- Flow data yang real-time update ketika ada postingan atau komentar baru

---

## 📱 Struktur Navigasi Aplikasi

```
Login/Register
    ↓
Feed (Tavern Board)
    ├→ Search (inline)
    ├→ Profile (klik icon Person)
    │   └→ Post Detail (klik postingan)
    │       └→ Back to Profile
    ├→ Post Detail (klik postingan)
    │   └→ Back to Feed
    └→ Logout
```

---

## 🛠️ Teknologi yang Digunakan

- **Jetpack Compose** - UI Framework
- **Room Database** - Local persistence
- **Kotlin Coroutines & Flow** - Asynchronous programming
- **ViewModel** - State management
- **Material Design 3** - Design system

---

## 📦 File Structure

```
app/src/main/java/com/example/tavern/
├── data/
│   ├── CommentDao.kt
│   ├── CommentEntity.kt
│   ├── PostDao.kt          [UPDATED - search queries]
│   ├── PostEntity.kt
│   ├── TavernDatabase.kt   [UPDATED - version 4]
│   ├── TavernRepository.kt [UPDATED - new functions]
│   ├── UserDao.kt          [UPDATED - profile functions]
│   └── UserEntity.kt       [UPDATED - bio, avatar, joinedDate]
├── ui/
│   ├── ProfileScreen.kt    [NEW FILE]
│   ├── RegisterScreen.kt
│   ├── TavernApp.kt        [UPDATED - search & profile nav]
│   ├── TavernViewModel.kt  [UPDATED - search & profile logic]
│   └── theme/
│       ├── Animations.kt
│       ├── Color.kt
│       ├── Shapes.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt
```

---

## 🎨 UI/UX Improvements

1. **Animasi Smooth** - Semua transisi menggunakan animasi slide dan fade
2. **Search Real-time** - Hasil pencarian update saat mengetik
3. **Loading States** - Indicator loading untuk operasi async
4. **Error Handling** - Pesan error yang informatif
5. **Empty States** - Ilustrasi dan pesan untuk kondisi kosong
6. **Clickable Elements** - Visual feedback untuk interaksi
7. **Material Design 3** - Konsisten dengan design system modern

---

## 🔄 Database Migration

**Version History:**
- V1: Initial schema (Posts, Users)
- V2: Added Comments
- V3: Enhanced Comments
- V4: Enhanced Users (bio, avatarUrl, joinedDate) ← **Current**

**Migration Strategy:**
- Menggunakan `fallbackToDestructiveMigration()`
- Database akan di-recreate otomatis saat version berubah
- **Note:** Data akan hilang saat upgrade. Untuk production, gunakan proper migration.

---

## ✅ Testing Checklist

### Profil
- [ ] Buat akun baru → Cek tanggal bergabung muncul
- [ ] Edit bio → Cek bio tersimpan
- [ ] Lihat histori postingan → Cek semua postingan muncul
- [ ] Klik postingan di profil → Cek navigasi ke diskusi

### Search
- [ ] Klik icon search → Cek search bar muncul
- [ ] Ketik kata kunci → Cek hasil muncul real-time
- [ ] Cari kata yang tidak ada → Cek "No posts found" muncul
- [ ] Hapus pencarian → Cek kembali ke feed utama
- [ ] Klik hasil pencarian → Cek buka detail postingan

### Multi-User
- [ ] Login dengan 2 akun berbeda
- [ ] Buat postingan dari akun 1
- [ ] Login dengan akun 2 → Cek postingan akun 1 muncul
- [ ] Komentar di postingan akun 1 → Cek komentar dengan nama akun 2
- [ ] Klik nama author → Cek buka profil author

---

## 🚀 Cara Menjalankan

1. Buka project di Android Studio
2. Sync Gradle
3. **Uninstall app lama jika ada** (karena database schema berubah)
4. Run aplikasi
5. Register akun baru
6. Test semua fitur

---

## 📝 Catatan Penting

1. **Database Reset**: Karena schema berubah, app perlu di-uninstall dulu sebelum install versi baru
2. **Avatar**: Saat ini hanya menampilkan inisial nama, bisa diperluas untuk upload foto
3. **Search**: Saat ini hanya search title dan content, bisa diperluas ke author atau tag
4. **Profile**: Avatar URL belum fully implemented, masih menggunakan inisial

---

## 🔮 Future Enhancements

- Upload foto profil real
- Filter pencarian advanced (by date, by author, by upvotes)
- Sort options (newest, popular, trending)
- User following system
- Notifications
- Like/unlike system
- Save favorite posts
- Share posts
- Edit/delete own posts
- Report system

---

Made with ❤️ for PAB Project
