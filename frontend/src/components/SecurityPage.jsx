import { useEffect, useState } from 'react'
import QRCode from 'qrcode'
import api from '../api/client'

/**
 * Autoservicio de 2FA: cualquier usuario logueado (sin importar el rol)
 * puede activar o desactivar el segundo factor para SU PROPIA cuenta
 * desde aca. El backend nunca ve la app de authenticator del usuario ni
 * un codigo QR real -- solo genera el secreto y la URI otpauth://; el QR
 * en si se dibuja aca mismo, en el navegador, con la libreria "qrcode".
 */
export default function SecurityPage({ notify }) {
  const [habilitado, setHabilitado] = useState(null)
  const [setup, setSetup] = useState(null) // { secret, otpauthUri }
  const [qrDataUrl, setQrDataUrl] = useState(null)
  const [codigo, setCodigo] = useState('')
  const [cargando, setCargando] = useState(true)
  const [enviando, setEnviando] = useState(false)

  const cargarEstado = () => {
    setCargando(true)
    api
      .get('/auth/2fa/estado')
      .then(({ data }) => setHabilitado(data.habilitado))
      .finally(() => setCargando(false))
  }

  useEffect(cargarEstado, [])

  const iniciarSetup = async () => {
    try {
      const { data } = await api.post('/auth/2fa/setup')
      setSetup(data)
      const dataUrl = await QRCode.toDataURL(data.otpauthUri, { margin: 1, width: 220 })
      setQrDataUrl(dataUrl)
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'No se pudo iniciar el setup de 2FA')
    }
  }

  const confirmarActivacion = async (e) => {
    e.preventDefault()
    setEnviando(true)
    try {
      await api.post('/auth/2fa/enable', { codigo })
      notify('success', '2FA activado correctamente')
      setSetup(null)
      setQrDataUrl(null)
      setCodigo('')
      cargarEstado()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'Codigo incorrecto')
    } finally {
      setEnviando(false)
    }
  }

  const desactivar = async (e) => {
    e.preventDefault()
    setEnviando(true)
    try {
      await api.post('/auth/2fa/disable', { codigo })
      notify('success', '2FA desactivado')
      setCodigo('')
      cargarEstado()
    } catch (err) {
      notify('error', err.response?.data?.mensaje || 'Codigo incorrecto')
    } finally {
      setEnviando(false)
    }
  }

  if (cargando) return <p className="empty">Cargando...</p>

  return (
    <div className="container-form">
      <h1>Mi cuenta</h1>
      <p className="subtitle-page">
        Verificacion en dos pasos (2FA) con una app de autenticacion (Google Authenticator, Authy, etc).
      </p>

      <div className="form-card">
        <p className="hint" style={{ marginTop: 0 }}>
          Estado actual:{' '}
          {habilitado ? (
            <span className="estado-ok">Activado</span>
          ) : (
            <span className="estado-bloqueado">Desactivado</span>
          )}
        </p>

        {!habilitado && !setup && (
          <button className="btn btn-primary" onClick={iniciarSetup}>
            Activar 2FA
          </button>
        )}

        {!habilitado && setup && (
          <form onSubmit={confirmarActivacion}>
            <p className="hint">
              Escaneá este codigo con tu app de autenticacion, o cargá el secreto a mano:
            </p>
            {qrDataUrl && <img src={qrDataUrl} alt="QR 2FA" className="preview-actual" />}
            <p className="hint">
              Secreto: <code>{setup.secret}</code>
            </p>
            <label htmlFor="codigo">Codigo de 6 digitos (para confirmar)</label>
            <input
              id="codigo"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
              inputMode="numeric"
              maxLength={6}
              placeholder="000000"
              required
            />
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={enviando}>
                {enviando ? 'Confirmando...' : 'Confirmar y activar'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => setSetup(null)}>
                Cancelar
              </button>
            </div>
          </form>
        )}

        {habilitado && (
          <form onSubmit={desactivar}>
            <label htmlFor="codigoDesactivar">Codigo actual (para confirmar que sos vos)</label>
            <input
              id="codigoDesactivar"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
              inputMode="numeric"
              maxLength={6}
              placeholder="000000"
              required
            />
            <div className="form-actions">
              <button type="submit" className="btn btn-danger" disabled={enviando}>
                {enviando ? 'Desactivando...' : 'Desactivar 2FA'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
