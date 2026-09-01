import { useEffect, useState } from 'react'
import api from '../api/client'

export default function AdminDashboard({ navigate }) {
  const [stats, setStats] = useState(null)

  useEffect(() => {
    api.get('/admin/stats').then(({ data }) => setStats(data))
  }, [])

  if (!stats) return <p className="empty">Cargando...</p>

  return (
    <>
      <h1>Panel de administracion</h1>
      <p className="subtitle-page">
        Solo visible para usuarios con categoria ADMIN (protegido por Spring Security en el backend).
      </p>

      <div className="stats-grid">
        <div className="stat-card">
          <span className="stat-num">{stats.totalUsuarios}</span>
          <span className="stat-label">Usuarios totales</span>
        </div>
        <div className="stat-card">
          <span className="stat-num">{stats.totalAdmins}</span>
          <span className="stat-label">Admins</span>
        </div>
        <div className="stat-card">
          <span className="stat-num">{stats.totalVendedores}</span>
          <span className="stat-label">Vendedores</span>
        </div>
        <div className="stat-card">
          <span className="stat-num">{stats.totalCompradores}</span>
          <span className="stat-label">Compradores</span>
        </div>
        <div className="stat-card">
          <span className="stat-num">{stats.totalJuegos}</span>
          <span className="stat-label">Juegos publicados</span>
        </div>
        <div className="stat-card">
          <span className="stat-num">{stats.totalSecciones}</span>
          <span className="stat-label">Secciones</span>
        </div>
      </div>

      <h2>Funciones de administrador</h2>
      <div className="admin-actions">
        <button className="btn btn-primary" onClick={() => navigate('userForm')}>
          + Crear usuario y asignar categoria
        </button>
        <button className="btn btn-secondary" onClick={() => navigate('adminUsers')}>
          Gestionar usuarios
        </button>
        <button className="btn btn-secondary" onClick={() => navigate('adminSections')}>
          Crear / eliminar secciones
        </button>
        <button className="btn btn-secondary" onClick={() => navigate('catalog')}>
          Moderar catalogo
        </button>
      </div>
    </>
  )
}
