import { useEffect, useState } from 'react'
import api, { resolveImageUrl } from '../api/client'

export default function CatalogPage({ user, navigate, notify }) {
  const [juegos, setJuegos] = useState([])
  const [loading, setLoading] = useState(true)

  const load = async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/juegos')
      setJuegos(data)
    } catch {
      notify('error', 'No se pudo cargar el catalogo')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleDelete = async (id) => {
    if (!window.confirm('Seguro que queres eliminar este juego?')) return
    try {
      await api.delete(`/juegos/${id}`)
      notify('success', 'Juego eliminado')
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo eliminar el juego')
    }
  }

  return (
    <>
      <div className="page-header">
        <h1>Catalogo de juegos</h1>
        {(user.rol === 'VENDEDOR' || user.rol === 'ADMIN') && (
          <button className="btn btn-primary" onClick={() => navigate('gameForm')}>
            + Publicar juego
          </button>
        )}
      </div>

      {user.rol === 'COMPRADOR' && (
        <p className="info-banner">
          Estas viendo el catalogo en modo <strong>solo lectura</strong>. Como comprador podes ver los
          juegos, pero no publicarlos ni editarlos.
        </p>
      )}

      {loading ? (
        <p className="empty">Cargando catalogo...</p>
      ) : (
        <div className="grid-juegos">
          {juegos.map((j) => (
            <div className="card-juego" key={j.id}>
              <div className="card-imagen">
                {j.imagenUrl ? (
                  <img src={resolveImageUrl(j.imagenUrl)} alt={j.nombre} />
                ) : (
                  <div className="sin-imagen">Sin imagen</div>
                )}
              </div>
              <div className="card-body">
                <h3>{j.nombre}</h3>
                <p className="precio">$ {Number(j.precio).toFixed(2)}</p>
                <p className="descripcion">{j.descripcion}</p>
                <p className="meta">
                  {j.seccion && <span className="tag">{j.seccion.nombre}</span>}
                  <span className="vendedor-nombre">
                    por <strong>{j.vendedor.nombreCompleto}</strong>
                  </span>
                </p>
                {j.puedeEditar && (
                  <div className="card-actions">
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => navigate('gameForm', { id: j.id })}
                    >
                      Editar
                    </button>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(j.id)}>
                      Eliminar
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
          {juegos.length === 0 && <p className="empty">Todavia no hay juegos publicados.</p>}
        </div>
      )}
    </>
  )
}
