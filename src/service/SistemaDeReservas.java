package service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import model.Sala;
import model.Pessoa;
import model.Reserva;
import model.StatusReserva;

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

    /*/
    public List<Sala> listarSalasDisponiveis(Date dataInicio, Date dataFinal) {
        List<Sala> disponiveis = new ArrayList<>();

        for(Reserva reserva: reservas) {
            boolean ocupada = false;

        }
    }*/

    public Reserva reservarSala(Sala sala, Pessoa pessoa, Date data) {
       Reserva novaReserva = new Reserva(data, StatusReserva.PENDENTE, sala, pessoa);

       boolean conflito = false;
       //Verificar colisões
       
       if(conflito) {
        throw new RuntimeException("Sala já reservada");
       } else {
        reservas.add(novaReserva);
        return novaReserva;
       }
    }
}
