# HYB-070 — Media Pipeline

## Boundary

Media disimpan sebagai object tenant-scoped. Chat service hanya menyimpan `mediaId` dan metadata yang diperlukan. Upload dan download memakai media service, bukan endpoint chat biasa. Storage key tidak boleh dapat ditebak dari URL client.

## Lifecycle

Object baru berada pada state `pending`. Setelah validasi ukuran, MIME type, hash, dan malware scan, object menjadi `available` atau `blocked`. Object yang gagal scan masuk `quarantined` dan tidak dapat di-download. Deletion mengubah state menjadi `deleted` dan menghapus object storage sesuai retention policy.

## Signed URLs

Presigned URL harus HTTPS, tenant-authorized, berumur pendek, dan dibatasi oleh operasi upload atau download. Upload wajib mencocokkan ukuran maksimum dan content type yang dikeluarkan service. Download hanya diberikan untuk object `available` kepada principal yang berwenang.

## Validation defaults

Batas kontrak default adalah 50 MiB per object. Implementasi dapat menetapkan batas lebih rendah berdasarkan jenis fitur. MIME type dari client tidak boleh menjadi satu-satunya dasar keputusan; server harus memeriksa content signature dan hasil scanner.

## Acceptance criteria

| Area | Kriteria |
|---|---|
| Isolation | Tenant lain tidak dapat membuat signed URL atau membaca metadata |
| Upload | Size, MIME, hash, dan expiry divalidasi |
| Security | Object pending/quarantined/blocked tidak dapat di-download |
| Scan | Malware result tercatat dan blocked object tidak bocor |
| Download | URL HTTPS, short-lived, dan operation-scoped |
| Thumbnail | Thumbnail memakai mediaId terpisah dan tidak melewati batas asli |
| Retention | Expiry dan deletion menghapus metadata serta object sesuai policy |
| Audit | Upload, scan result, download grant, delete, dan restore tercatat |
