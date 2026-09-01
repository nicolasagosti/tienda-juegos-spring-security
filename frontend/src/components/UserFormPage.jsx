import { useEffect, useState } from 'react'
import api from '../api/client'

const ROLES = ['ADMIN', 'VENDEDOR', 'COMPRADOR']

export default function UserFormPage({ userId, navigate, notify }) {
  const esNuevo = !userId
  const [form, setForm] = useState({
    username: '',
    password: '',
    nombreCompleto: '',
    email: '',
    rol: 'COMPRADOR',
    habilitado: true,
    nuevaPassword: '',
  })
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(!esNuevo)

  useEffect(() => {
    if (!esNuevo) {
      api
        .get('/usuarios')
        .then(({ data }) => {
          const u = data.find((x) => x.id === userId)
          if (u) setForm((f) => ({ ...f, ...u, nuevaPassword: '' }))
        })
        .finally(() => setLoading(false))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId])

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (esNuevo) {
        await api.post('/usuarios', {
          username: form.username,
          password: form.password,
          nombreCompleto: form.nombreCompleto,
          email: form.email,
          rol: form.rol,
        })
        notify('success', `Usuario creado y categoria asignada: ${form.rol}`)
      } else {
        await api.put(`/usuarios/${userId}`, {
          nombreCompleto: form.nombreCompleto,
          email: form.email,
          rol: form.rol,
          habilitado: form.habilitado,
          nuevaPassword: form.nuevaPassword,
        })
        notify('success', 'Perfil actualizado')
      }
      navigate('adminUsers')
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo guardar el usuario')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="empty">Cargando...</p>

  return (
    <div className="container-form">
      <h1>{esNuevo ? 'Crear nuevo usuario' : 'Editar usuario'}</h1>
      {esNuevo && (
        <p className="subtitle-page">Al crearlo le asignas directamente una categoria (rol).</p>
      )}

      <form onSubmit={handleSubmit} className="form-card">
        <label htmlFor="username">Usuario (login)</label>
        <input
          id="username"
          name="username"
          value={form.username}
          onChange={handleChange}
          readOnly={!esNuevo}
          required
        />

        {esNuevo && (
          <>
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </>
        )}

        {!esNuevo && (
          <>
            <label htmlFor="nuevaPassword">Nueva contraseña (dejar vacio para no cambiarla)</label>
            <input
              id="nuevaPassword"
              name="nuevaPassword"
              type="password"
              value={form.nuevaPassword}
              onChange={handleChange}
            />
          </>
        )}

        <label htmlFor="nombreCompleto">Nombre completo</label>
        <input
          id="nombreCompleto"
          name="nombreCompleto"
          value={form.nombreCompleto}
          onChange={handleChange}
          required
        />

        <label htmlFor="email">Email</label>
        <input
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          required
        />

        <label htmlFor="rol">Categoria (rol)</label>
        <select id="rol" name="rol" value={form.rol} onChange={handleChange} required>
          {ROLES.map((r) => (
            <option key={r} value={r}>
              {r}
            </option>
          ))}
        </select>

        {!esNuevo && (
          <div className="checkbox-row">
            <input
              type="checkbox"
              id="habilitado"
              name="habilitado"
              checked={form.habilitado}
              onChange={handleChange}
            />
            <label htmlFor="habilitado">Cuenta habilitada</label>
          </div>
        )}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('adminUsers')}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
