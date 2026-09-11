package com.donatrack.incentivos.infrastructure.adapters.out.persistence.mappers;

import com.donatrack.incentivos.domain.entities.Insignia;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import com.donatrack.incentivos.domain.entities.RegistroDonacion;
import com.donatrack.incentivos.domain.entities.categoria.CategoriaDonante;
import com.donatrack.incentivos.domain.entities.misiones.Mision;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.InsigniaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.MisionCompletadaEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.PerfilDonanteEntity;
import com.donatrack.incentivos.infrastructure.adapters.out.persistence.entities.RegistroDonacionEntity;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerfilDonanteMapper {

    public static PerfilDonanteEntity toEntity(PerfilDonante domain) {
        if (domain == null) return null;

        PerfilDonanteEntity entity = new PerfilDonanteEntity();
        entity.setDonanteId(domain.getDonanteId());
        entity.setCategoria(domain.getCategoria().name());
        entity.setFechaCorteRacha(domain.getMetricas().getFechaCorteRacha());

        // Insignias
        if (domain.getInsigniasObtenidas() != null) {
            entity.setInsigniasObtenidas(domain.getInsigniasObtenidas().stream().map(ins -> {
                InsigniaEntity ie = new InsigniaEntity();
                ie.setId(ins.getId());
                ie.setPerfil(entity);
                ie.setNombre(ins.getNombre());
                ie.setDescripcion(ins.getDescripcion());
                ie.setUrlImagen(ins.getUrlImagen());
                ie.setFechaObtencion(ins.getFechaObtencion());
                ie.setVisiblePublicamente(ins.isVisiblePublicamente());
                return ie;
            }).collect(Collectors.toList()));
        }

        // Registros Donacion
        if (domain.getMetricas().getRegistrosDonacion() != null) {
            entity.setRegistrosDonacion(domain.getMetricas().getRegistrosDonacion().stream().map(reg -> {
                RegistroDonacionEntity re = new RegistroDonacionEntity();
                // We use auto-generated ID for RegistroDonacionEntity so we leave it null here if it's new
                // For simplicity, we can let JPA generate the UUID on insert. Wait, we don't have domain IDs for RegistroDonacion mapping.
                re.setPerfil(entity);
                re.setIdDonacion(reg.getIdDonacion());
                re.setCantidadBienes(reg.getCantidadBienes());
                re.setCategorias(reg.getCategorias() != null ? new HashSet<>(reg.getCategorias()) : new HashSet<>());
                re.setIdEntidadBeneficiaria(reg.getIdEntidadBeneficiaria());
                re.setMesDonacion(reg.getMesDonacion() != null ? reg.getMesDonacion().toString() : null);
                re.setFechaDonacion(reg.getFechaDonacion());
                return re;
            }).collect(Collectors.toList()));
        }

        // Misiones Completadas
        if (domain.getMetricas().getMisionesCompletadas() != null) {
            entity.setMisionesCompletadas(domain.getMetricas().getMisionesCompletadas().entrySet().stream().map(entry -> {
                MisionCompletadaEntity mc = new MisionCompletadaEntity();
                mc.setPerfil(entity);
                mc.setNombreMision(entry.getKey().getNombre());
                mc.setMesCompletado(entry.getValue().toString());
                return mc;
            }).collect(Collectors.toList()));
        }

        return entity;
    }

    public static PerfilDonante toDomain(PerfilDonanteEntity entity) {
        if (entity == null) return null;

        // The constructor creates the metricas, lists, and calls cargarMisionesDeCategoriaActual
        PerfilDonante domain = new PerfilDonante(entity.getDonanteId());
        
        // Categoria
        if (entity.getCategoria() != null) {
            domain.setCategoria(CategoriaDonante.valueOf(entity.getCategoria()));
            // Reload misiones because category might have changed from COLABORADOR (default in constructor)
            domain.cargarMisionesDeCategoriaActual();
        }

        domain.getMetricas().setFechaCorteRacha(entity.getFechaCorteRacha());

        // Insignias
        if (entity.getInsigniasObtenidas() != null) {
            domain.setInsigniasObtenidas(entity.getInsigniasObtenidas().stream().map(ie -> {
                Insignia ins = new Insignia(ie.getNombre(), ie.getDescripcion());
                ins.setId(ie.getId());
                ins.setUrlImagen(ie.getUrlImagen());
                ins.setFechaObtencion(ie.getFechaObtencion());
                ins.setVisiblePublicamente(ie.getVisiblePublicamente() != null ? ie.getVisiblePublicamente() : true);
                return ins;
            }).collect(Collectors.toList()));
        }

        // Registros Donacion
        if (entity.getRegistrosDonacion() != null) {
            domain.getMetricas().setRegistrosDonacion(entity.getRegistrosDonacion().stream().map(re -> {
                RegistroDonacion reg = new RegistroDonacion();
                reg.setIdDonacion(re.getIdDonacion());
                reg.setCantidadBienes(re.getCantidadBienes() != null ? re.getCantidadBienes() : 0);
                reg.setCategorias(re.getCategorias() != null ? new HashSet<>(re.getCategorias()) : new HashSet<>());
                reg.setIdEntidadBeneficiaria(re.getIdEntidadBeneficiaria());
                reg.setMesDonacion(re.getMesDonacion() != null ? YearMonth.parse(re.getMesDonacion()) : null);
                reg.setFechaDonacion(re.getFechaDonacion());
                return reg;
            }).collect(Collectors.toList()));
        }

        // Misiones Completadas
        if (entity.getMisionesCompletadas() != null) {
            Map<Mision, YearMonth> map = domain.getMetricas().getMisionesCompletadas();
            for (MisionCompletadaEntity mce : entity.getMisionesCompletadas()) {
                // We create a dummy Mision with just the name, which is sufficient 
                // for the Map because we added @EqualsAndHashCode(of = "nombre") to Mision!
                Mision dummy = new Mision(mce.getNombreMision(), null, null, 0);
                map.put(dummy, YearMonth.parse(mce.getMesCompletado()));
            }

            // Restore queue state by popping already completed missions
            // Since Map contains dummy missions, containsKey will correctly match by 'nombre'
            while (domain.getMisionActual() != null && map.containsKey(domain.getMisionActual())) {
                domain.setMisionActual(domain.getMisionesPendientes().poll());
            }
        }

        return domain;
    }
}
