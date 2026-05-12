package service;

import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import model.Sala;
import model.Pessoa;
import model.Reserva;
import model.StatusReserva;
import strategy.PoliticaDeReserva;
import strategy.PrimeiroAReservar;
import strategy.PrioridadeParaDocente;

public class SistemaDeReservas {
    private static SistemaDeReservas instancia;

    private List<Sala> salas;
    private List<Reserva> reservas;

    private SistemaDeReservas() {
        salas = new ArrayList<>();
        reservas = new ArrayList<>();
    }

    public static synchronized SistemaDeReservas getInstance() {
        if (instancia == null) {
            instancia = new SistemaDeReservas();
        }

        return instancia;
    }

    private Reserva achaReserva(Sala sala, Date data) {
        for(Reserva reserva : reservas) {
            if(reserva.getSala().equals(sala) && reserva.getDia().equals(data)) {
                return reserva;
            }
        }

        return null;
    }

    private List<Reserva> sortReservas(List<Reserva> aOrdenar) {
        aOrdenar.sort(
            Comparator
                .comparing(Reserva::getDia)
                .thenComparing(
                    reserva -> reserva.getSala().getNumero()
                )
        );

        return aOrdenar;
    }

    public List<Sala> listarSalasDisponiveis(Date dataInicio, Date dataFinal) {
        List<Sala> disponiveis = new ArrayList<>();

        for(Sala sala : salas) {
            boolean ocupada = false;
            
            for(Reserva reserva : reservas) {
                boolean mesmaSala = reserva.getSala().equals(sala);
                boolean dentroIntervalo = !reserva.getDia().before(dataInicio) && !reserva.getDia().after(dataFinal);

                if(mesmaSala && dentroIntervalo) {
                    ocupada = true;
                    break;
                }
            }

            if(!ocupada) {
                disponiveis.add(sala);
            }
        }

        return disponiveis;
    }

    public Resultado reservarSala(Sala sala, Pessoa pessoa, Date data) {
        Reserva novaReserva = new Reserva(LocalDateTime.now(), data, StatusReserva.PENDENTE, sala, pessoa);
        Reserva jaExiste = achaReserva(sala, data);
        PoliticaDeReserva politica = new PrimeiroAReservar(); 
       
        if(jaExiste != null) {
            if(politica.verificaColisao(jaExiste, novaReserva) == jaExiste) {
                politica = new PrioridadeParaDocente();
                if(politica.verificaColisao(jaExiste, novaReserva) == jaExiste) {
                    return Resultado.JA_EXISTE;
                }
            }

            jaExiste.notificarTodos(jaExiste);
            reservas.remove(jaExiste);
        }

        reservas.add(novaReserva);
        return Resultado.SUCESSO;
    }

    public Resultado cancelarReserva(Sala sala, Pessoa pessoa, Date data) {
        Reserva reserva = achaReserva(sala, data);

        if(reserva == null){
            return Resultado.NAO_ENCONTRADO;
        }

        if(!reserva.getPessoa().equals(pessoa)) {
            return Resultado.NAO_AUTORIZADO;
        }
        
        reservas.remove(reserva);
        return Resultado.SUCESSO;
    }

    public Resultado editarReserva(Sala sala, Pessoa pessoa, Date data, Sala novaSala, Date novaData) {
        Reserva aEditar = achaReserva(sala, data);

        if(aEditar == null){
            return Resultado.NAO_ENCONTRADO;
        }

        if(!aEditar.getPessoa().equals(pessoa)) {
            return Resultado.NAO_AUTORIZADO;
        }


        reservas.remove(aEditar);
        
        Resultado res = reservarSala(novaSala, pessoa, novaData);
        if(res.equals(Resultado.JA_EXISTE)) {
            reservas.add(aEditar);
        }

        return res;
    }

    public List<Reserva> relatorioDiario(Date data) {
        List<Reserva> relatorio = new ArrayList<>();
        
        for(Reserva reserva : reservas) {
            if(reserva.getDia().equals(data)) {
                relatorio.add(reserva);
            }
        }

        return sortReservas(relatorio);
    }
}