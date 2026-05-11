package service;

import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

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

    private Reserva achaReserva(Sala sala, Date data) {
        for(Reserva reserva : reservas) {
            if(reserva.getSala().equals(sala) && reserva.getDia().equals(data)) {
                return reserva;
            }
        }

        return null;
    }

    public Reserva reservarSala(Sala sala, Pessoa pessoa, Date data) {
        Reserva novaReserva = new Reserva(LocalDateTime.now(), data, StatusReserva.PENDENTE, sala, pessoa);
        Reserva jaExiste = achaReserva(sala, data);

        PoliticaDeReserva politica = new PrimeiroAReservar(); 
       
        if(jaExiste == null) {
            reservas.add(novaReserva);
            return novaReserva;
        }
        
        if(politica.verificaColisao(jaExiste, novaReserva) == novaReserva) {
            jaExiste.notificarTodos(jaExiste);
            reservas.remove(jaExiste);
            
            reservas.add(novaReserva);
            return novaReserva;
        }

        politica = new PrioridadeParaDocente();
        if(politica.verificaColisao(jaExiste, novaReserva) == novaReserva) {
            jaExiste.notificarTodos(jaExiste);
            reservas.remove(jaExiste);

            reservas.add(novaReserva);
            return novaReserva;
        }
        
       return null;
    }

    public void cancelarReserva(Reserva reserva) {
        if(achaReserva(reserva.getSala(), reserva.getDia()) == null){
            System.out.println("Reserva não encontrada");
        } else {
            reservas.remove(reserva);
            System.out.println("Reserva cancelada com sucesso");
        }
    }
}
