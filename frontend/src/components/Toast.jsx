import { useEffect } from 'react'

/** Notificacion flotante temporal para confirmar o reportar errores de una accion. */
export default function Toast({ toast, onDone }) {
  useEffect(() => {
    if (!toast) return
    const timer = setTimeout(onDone, 3200)
    return () => clearTimeout(timer)
  }, [toast, onDone])

  if (!toast) return null

  return (
    <div className={`toast toast-${toast.type}`} key={toast.key}>
      {toast.message}
    </div>
  )
}
