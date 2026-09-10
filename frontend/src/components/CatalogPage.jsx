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

  const handleComprar = async (id) => {
    try {
      await api.post(`/juegos/${id}/comprar`)
      notify('success', 'Compra realizada (ficticia)')
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo completar la compra')
    }
  }

  const handleAgregarStock = async (id) => {
    const val = window.prompt('Cuantas unidades agregar al stock?', '5')
    if (val == null) return
    const cantidad = Number(val)
    if (!Number.isInteger(cantidad) || cantidad <= 0) {
      notify('error', 'Ingresa un numero entero mayor a 0')
      return
    }
    try {
      await api.post(`/juegos/${id}/stock`, null, { params: { cantidad } })
      notify('success', `Stock actualizado (+${cantidad})`)
      load()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo actualizar el stock')
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
          Como comprador podes <strong>comprar</strong> juegos (compra ficticia, sin pago real) y verlos
          en <strong>Mis compras</strong>. No podes publicarlos ni editarlos.
        </p>
      )}

      {loading ? (
        <p className="empty">Cargando catalogo...</p>
      ) : (
        <div className="grid-juegos">
          {juegos.map((j) => (
            <div className={`card-juego${j.stock <= 0 ? ' card-agotado' : ''}`} key={j.id}>
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
                  <span className={j.stock > 0 ? 'estado-ok' : 'estado-bloqueado'}>
                    {j.stock > 0 ? `${j.stock} en stock` : 'Sin stock'}
                  </span>
                  <span className="vendedor-nombre">
                    por <strong>{j.vendedor.nombreCompleto}</strong>
                  </span>
                </p>
                {j.puedeEditar && (
                  <div className="card-actions">
                    <button className="btn btn-sm" onClick={() => handleAgregarStock(j.id)}>
                      + Stock
                    </button>
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
                {user.rol === 'COMPRADOR' && (
                  <div className="card-actions">
                    {j.comprado ? (
                      <button className="btn btn-secondary btn-sm" disabled>
                        ✓ Comprado
                      </button>
                    ) : j.stock > 0 ? (
                      <button className="btn btn-primary btn-sm" onClick={() => handleComprar(j.id)}>
                        Comprar
                      </button>
                    ) : (
                      <button className="btn btn-secondary btn-sm" disabled>
                        Sin stock
                      </button>
                    )}
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
