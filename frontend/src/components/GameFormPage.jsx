import { useEffect, useState } from 'react'
import api from '../api/client'

export default function GameFormPage({ gameId, navigate, notify }) {
  const esNuevo = !gameId
  const [secciones, setSecciones] = useState([])
  const [form, setForm] = useState({ nombre: '', descripcion: '', precio: '', seccionId: '' })
  const [imagenActual, setImagenActual] = useState(null)
  const [archivo, setArchivo] = useState(null)
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(!esNuevo)

  useEffect(() => {
    api.get('/secciones').then(({ data }) => setSecciones(data))
    if (!esNuevo) {
      api
        .get(`/juegos/${gameId}`)
        .then(({ data }) => {
          setForm({
            nombre: data.nombre,
            descripcion: data.descripcion || '',
            precio: data.precio,
            seccionId: data.seccion ? String(data.seccion.id) : '',
          })
          setImagenActual(data.imagenUrl)
        })
        .catch(() => notify('error', 'No se pudo cargar el juego'))
        .finally(() => setLoading(false))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [gameId])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    const data = new FormData()
    data.append('nombre', form.nombre)
    data.append('descripcion', form.descripcion)
    data.append('precio', form.precio)
    if (form.seccionId) data.append('seccionId', form.seccionId)
    if (archivo) data.append('imagen', archivo)

    try {
      if (esNuevo) {
        await api.post('/juegos', data)
        notify('success', 'Juego publicado correctamente')
      } else {
        await api.put(`/juegos/${gameId}`, data)
        notify('success', 'Juego actualizado correctamente')
      }
      navigate('catalog')
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo guardar el juego')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="empty">Cargando...</p>

  return (
    <div className="container-form">
      <h1>{esNuevo ? 'Publicar nuevo juego' : 'Editar juego'}</h1>
      <form onSubmit={handleSubmit} className="form-card" encType="multipart/form-data">
        <label htmlFor="nombre">Nombre del juego</label>
        <input id="nombre" name="nombre" value={form.nombre} onChange={handleChange} required />

        <label htmlFor="descripcion">Descripcion</label>
        <textarea
          id="descripcion"
          name="descripcion"
          rows="4"
          value={form.descripcion}
          onChange={handleChange}
        />

        <label htmlFor="precio">Precio (USD)</label>
        <input
          id="precio"
          name="precio"
          type="number"
          step="0.01"
          min="0"
          value={form.precio}
          onChange={handleChange}
          required
        />

        <label htmlFor="seccionId">Seccion / categoria</label>
        <select id="seccionId" name="seccionId" value={form.seccionId} onChange={handleChange}>
          <option value="">-- Sin seccion --</option>
          {secciones.map((s) => (
            <option key={s.id} value={s.id}>
              {s.nombre}
            </option>
          ))}
        </select>

        <label htmlFor="imagen">Imagen de portada</label>
        <input
          id="imagen"
          type="file"
          accept="image/*"
          onChange={(e) => setArchivo(e.target.files[0])}
        />
        {imagenActual && !archivo && (
          <>
            <p className="hint">Imagen actual:</p>
            <img src={imagenActual} className="preview-actual" alt="actual" />
          </>
        )}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Guardando...' : esNuevo ? 'Publicar' : 'Guardar cambios'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('catalog')}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
