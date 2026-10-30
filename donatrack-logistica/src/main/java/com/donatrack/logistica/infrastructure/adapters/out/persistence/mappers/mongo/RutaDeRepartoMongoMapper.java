package com.donatrack.logistica.infrastructure.adapters.out.persistence.mappers.mongo;

import com.donatrack.logistica.domain.entities.entregas.ComprobanteRecepcion;
import com.donatrack.logistica.domain.entities.entregas.Entrega;
import com.donatrack.logistica.domain.entities.reparto.Camion;
import com.donatrack.logistica.domain.entities.reparto.Chofer;
import com.donatrack.logistica.domain.entities.reparto.Coordenada;
import com.donatrack.logistica.domain.entities.reparto.Direccion;
import com.donatrack.logistica.domain.entities.reparto.Parada;
import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ComprobanteRecepcionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CoordenadaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.DireccionDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ParadaDocument;
import com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RutaDeRepartoMongoMapper {

  private RutaDeRepartoMongoMapper() {}

  // ==================== RUTA ====================

  public static RutaDeRepartoDocument toDocument(RutaDeReparto domain) {
    if (domain == null) return null;

    RutaDeRepartoDocument doc = new RutaDeRepartoDocument();
    doc.setId(domain.getId() != null ? domain.getId() : UUID.randomUUID());
    doc.setFechaOperativa(domain.getFechaOperativa());
    doc.setIniciada(domain.getIniciada() != null ? domain.getIniciada() : false);

    // Desnormalización de Camión
    if (domain.getCamion() != null) {
      Camion c = domain.getCamion();
      doc.setCamionId(c.getId());
      doc.setCamionPatente(c.getPatente());
      doc.setCamionCapacidadVolumen(c.getCapacidadVolumen());
      doc.setCamionAltura(c.getAltura());
      doc.setCamionCapacidadCarga(c.getCapacidadCarga());
    }

    // Desnormalización de Chofer
    if (domain.getChofer() != null) {
      Chofer ch = domain.getChofer();
      doc.setChoferId(ch.getId());
      doc.setChoferLegajo(ch.getLegajo());
      doc.setChoferNombre(ch.getNombre());
    }

    // Paradas embebidas
    if (domain.getParadas() != null) {
      List<ParadaDocument> paradasDocs = new ArrayList<>();
      for (Parada p : domain.getParadas()) {
        paradasDocs.add(toParadaDocument(p));
      }
      doc.setParadas(paradasDocs);
    } else {
      doc.setParadas(new ArrayList<>());
    }

    return doc;
  }

  public static RutaDeReparto toDomain(RutaDeRepartoDocument doc) {
    if (doc == null) return null;

    RutaDeReparto domain = new RutaDeReparto();
    domain.setId(doc.getId());
    domain.setFechaOperativa(doc.getFechaOperativa());
    domain.setIniciada(doc.getIniciada() != null ? doc.getIniciada() : false);

    // Reconstrucción de Camión a partir de datos desnormalizados
    if (doc.getCamionId() != null || doc.getCamionPatente() != null) {
      Camion camion = new Camion(
          doc.getCamionId(),
          doc.getCamionPatente(),
          doc.getCamionCapacidadVolumen(),
          doc.getCamionAltura(),
          doc.getCamionCapacidadCarga());
      domain.setCamion(camion);
    }

    // Reconstrucción de Chofer a partir de datos desnormalizados
    if (doc.getChoferId() != null || doc.getChoferLegajo() != null) {
      Chofer chofer = new Chofer(
          doc.getChoferId(),
          doc.getChoferLegajo(),
          doc.getChoferNombre());
      domain.setChofer(chofer);
    }

    // Reconstrucción de Paradas embebidas
    if (doc.getParadas() != null) {
      List<Parada> paradasDomain = new ArrayList<>();
      for (ParadaDocument pd : doc.getParadas()) {
        paradasDomain.add(toParadaDomain(pd));
      }
      domain.setParadas(paradasDomain);
    } else {
      domain.setParadas(new ArrayList<>());
    }

    return domain;
  }

  // ==================== PARADA ====================

  public static ParadaDocument toParadaDocument(Parada domain) {
    if (domain == null) return null;

    ParadaDocument doc = new ParadaDocument();
    doc.setOrden(domain.getOrden());

    if (domain.getDireccion() != null) {
      doc.setDireccion(new DireccionDocument(
          domain.getDireccion().getCalle(),
          domain.getDireccion().getAltura(),
          domain.getDireccion().getLocalidad()));
    }

    if (domain.getCoordenada() != null) {
      doc.setCoordenada(new CoordenadaDocument(
          domain.getCoordenada().getLatitud(),
          domain.getCoordenada().getLongitud()));
    }

    if (domain.getEntregas() != null) {
      List<EntregaDocument> entregasDocs = new ArrayList<>();
      for (Entrega e : domain.getEntregas()) {
        entregasDocs.add(toEntregaDocument(e));
      }
      doc.setEntregas(entregasDocs);
    } else {
      doc.setEntregas(new ArrayList<>());
    }

    return doc;
  }

  public static Parada toParadaDomain(ParadaDocument doc) {
    if (doc == null) return null;

    Parada domain = new Parada();
    domain.setOrden(doc.getOrden());

    if (doc.getDireccion() != null) {
      domain.setDireccion(new Direccion(
          doc.getDireccion().getCalle(),
          doc.getDireccion().getAltura(),
          doc.getDireccion().getLocalidad()));
    }

    if (doc.getCoordenada() != null) {
      domain.setCoordenada(new Coordenada(
          doc.getCoordenada().getLatitud(),
          doc.getCoordenada().getLongitud()));
    }

    if (doc.getEntregas() != null) {
      List<Entrega> entregasDomain = new ArrayList<>();
      for (EntregaDocument ed : doc.getEntregas()) {
        entregasDomain.add(toEntregaDomain(ed));
      }
      domain.setEntregas(entregasDomain);
    } else {
      domain.setEntregas(new ArrayList<>());
    }

    return domain;
  }

  // ==================== ENTREGA ====================

  public static EntregaDocument toEntregaDocument(Entrega domain) {
    if (domain == null) return null;

    EntregaDocument doc = new EntregaDocument();
    doc.setIdEntrega(domain.getIdEntrega() != null ? domain.getIdEntrega() : UUID.randomUUID());
    doc.setEstado(domain.getEstado());
    doc.setPesoEstimado(domain.getPesoEstimado());
    doc.setVolumenEstimado(domain.getVolumenEstimado());

    if (domain.getComprobanteRecepcion() != null) {
      ComprobanteRecepcion cr = domain.getComprobanteRecepcion();
      doc.setComprobanteRecepcion(new ComprobanteRecepcionDocument(
          cr.getFechaHora(),
          cr.getFotos() != null ? new ArrayList<>(cr.getFotos()) : new ArrayList<>(),
          cr.getCamionPatente()));
    }

    return doc;
  }

  public static Entrega toEntregaDomain(EntregaDocument doc) {
    if (doc == null) return null;

    Entrega domain = new Entrega();
    domain.setIdEntrega(doc.getIdEntrega());
    domain.setEstado(doc.getEstado());
    domain.setPesoEstimado(doc.getPesoEstimado());
    domain.setVolumenEstimado(doc.getVolumenEstimado());

    if (doc.getComprobanteRecepcion() != null) {
      ComprobanteRecepcionDocument crd = doc.getComprobanteRecepcion();
      domain.setComprobanteRecepcion(new ComprobanteRecepcion(
          crd.getFechaHora(),
          crd.getFotos() != null ? new ArrayList<>(crd.getFotos()) : new ArrayList<>(),
          crd.getCamionPatente()));
    }

    return domain;
  }
}
