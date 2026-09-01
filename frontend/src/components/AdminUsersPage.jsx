import { useEffect, useState } from 'react'
import api from '../api/client'

export default function AdminUsersPage({ navigate, notify }) {
  const [usuarios, setUsuarios] = useState([])
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    api
      .get('/usuarios')
      .then(({ data }) => setUsuarios(data))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const toggle = async (id) => {
    try {
      await api.post(`/usuarios/${id}/toggle`)
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo actualizar el estado')
    }
  }

  const eliminar = async (id) => {
    if (!window.confirm('Seguro que queres eliminar este usuario?')) return
    try {
      await api.delete(`/usuarios/${id}`)
      notify('success', 'Usuario eliminado')
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo eliminar')
    }
  }

  return (
    <>
      <div className="page-header">
        <h1>Usuarios</h1>
        <button className="btn btn-primary" onClick={() => navigate('userForm')}>
          + Nuevo usuario
        </button>
      </div>

      {loading ? (
        <p className="empty">Cargando...</p>
      ) : (
        <div className="tabla-wrap">
          <table className="tabla">
            <thead>
              <tr>
                <th>Usuario</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Categoria</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((u) => (
                <tr key={u.id}>
                  <td>{u.username}</td>
                  <td>{u.nombreCompleto}</td>
                  <td>{u.email}</td>
                  <td>
                    <span className={`badge badge-${u.rol.toLowerCase()}`}>{u.rol}</span>
                  </td>
                  <td>
                    {u.habilitado ? (
                      <span className="estado-ok">Habilitado</span>
                    ) : (
                      <span className="estado-bloqueado">Deshabilitado</span>
                    )}
                  </td>
                  <td className="acciones">
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => navigate('userForm', { id: u.id })}
                    >
                      Editar
                    </button>
                    <button className="btn btn-sm" onClick={() => toggle(u.id)}>
                      {u.habilitado ? 'Deshabilitar' : 'Habilitar'}
                    </button>
                    <button className="btn btn-danger btn-sm" onClick={() => eliminar(u.id)}>
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
