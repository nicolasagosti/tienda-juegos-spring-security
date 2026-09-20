import { useEffect, useState } from 'react'
import api, { resolveImageUrl } from '../api/client'

export default function MisComprasPage({ notify }) {
  const [compras, setCompras] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api
      .get('/compras')
      .then(({ data }) => setCompras(data))
      .catch(() => notify('error', 'No se pudieron cargar tus compras'))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const total = compras.reduce((acc, c) => acc + Number(c.precio), 0)

  return (
    <>
      <div className="page-header">
        <h1>Mis compras</h1>
      </div>

      {loading ? (
        <p className="empty">Cargando...</p>
      ) : compras.length === 0 ? (
        <p className="empty">Todavia no compraste ningun juego.</p>
      ) : (
        <>
          <p className="info-banner">
            {compras.length} {compras.length === 1 ? 'juego comprado' : 'juegos comprados'} · total
            gastado (ficticio) <strong>$ {total.toFixed(2)}</strong>
          </p>
          <div className="grid-juegos">
            {compras.map((c) => (
              <div className="card-juego" key={c.id}>
                <div className="card-imagen">
                  {c.imagenUrl ? (
                    <img src={resolveImageUrl(c.imagenUrl)} alt={c.nombreJuego} />
                  ) : (
                    <div className="sin-imagen">Sin imagen</div>
                  )}
                </div>
                <div className="card-body">
                  <h3>{c.nombreJuego}</h3>
                  <p className="precio">$ {Number(c.precio).toFixed(2)}</p>
                  <p className="meta">
                    <span className="vendedor-nombre">
                      comprado el {new Date(c.fechaCompra).toLocaleDateString()}
                    </span>
                  </p>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </>
  )
}
