import { useEffect, useState } from 'react'
import api from '../api/client'

export default function AdminSectionsPage({ notify }) {
  const [secciones, setSecciones] = useState([])
  const [nombre, setNombre] = useState('')
  const [descripcion, setDescripcion] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    api
      .get('/secciones')
      .then(({ data }) => setSecciones(data))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const crear = async (e) => {
    e.preventDefault()
    try {
      await api.post('/secciones', { nombre, descripcion })
      setNombre('')
      setDescripcion('')
      notify('success', 'Seccion creada')
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo crear la seccion')
    }
  }

  const eliminar = async (id) => {
    if (!window.confirm('Seguro que queres eliminar esta seccion?')) return
    try {
      await api.delete(`/secciones/${id}`)
      notify('success', 'Seccion eliminada')
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo eliminar')
    }
  }

  return (
    <>
      <h1>Secciones del catalogo</h1>
      <p className="subtitle-page">
        Las secciones son las categorias en las que los vendedores clasifican sus juegos.
      </p>

      <form onSubmit={crear} className="form-card form-inline">
        <input
          placeholder="Nombre de la seccion"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          required
        />
        <input
          placeholder="Descripcion (opcional)"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
        />
        <button type="submit" className="btn btn-primary">
          + Crear seccion
        </button>
      </form>

      {loading ? (
        <p className="empty">Cargando...</p>
      ) : (
        <div className="tabla-wrap">
          <table className="tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Descripcion</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {secciones.map((s) => (
                <tr key={s.id}>
                  <td>{s.nombre}</td>
                  <td>{s.descripcion}</td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={() => eliminar(s.id)}>
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
