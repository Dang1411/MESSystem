<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f0f2f5;
  color: #333;
}

/* Scrollbar tùy chỉnh */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: #f1f1f1; }
::-webkit-scrollbar-thumb { background: #888; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #555; }

/* Badge trạng thái */
.badge-status {
  font-size: 0.78rem;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.badge-waiting    { background: #6c757d; color: #fff; }
.badge-inprogress { background: #0d6efd; color: #fff; }
.badge-ok         { background: #198754; color: #fff; }
.badge-ng         { background: #dc3545; color: #fff; }
.badge-rework     { background: #fd7e14; color: #fff; }
.badge-scrap      { background: #6f42c1; color: #fff; }
.badge-hold       { background: #ffc107; color: #000; }
.badge-finished   { background: #0dcaf0; color: #000; }

.badge-created    { background: #6c757d; color: #fff; }
.badge-completed  { background: #198754; color: #fff; }
.badge-cancelled  { background: #dc3545; color: #fff; }
.badge-active     { background: #198754; color: #fff; }
.badge-inactive   { background: #6c757d; color: #fff; }

/* Card công nghiệp */
.card-mes {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: box-shadow 0.2s;
}
.card-mes:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); }

/* Table */
.table-mes thead th {
  background: #1a3a5c;
  color: #fff;
  font-weight: 600;
  border: none;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.table-mes tbody tr:hover { background: #e8f4fd; }

/* Btn */
.btn-mes { border-radius: 6px; font-weight: 500; }

/* Toast notification */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
}
</style>
