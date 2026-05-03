package com.grpc.clima.cliente;

import com.grpc.clima.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WeatherController {

    
    @GrpcClient("weatherService")
    private WeatherServiceGrpc.WeatherServiceBlockingStub weatherStub;

    @GetMapping("/temperatura")
    public String getTemperatura(@RequestParam String cidade) {
        CidadeRequest request = CidadeRequest.newBuilder()
                .setNome(cidade)
                .build();

        TemperaturaResponse response = weatherStub.obterTemperaturaAtual(request);

        return "A temperatura em " + response.getNome() + " é: " + response.getTemperatura() + "°C";
    }

    @GetMapping("/cidades")
    public List<String> listarCidades() {
        ListaCidadesResponse response = weatherStub.listarCidades(Empty.newBuilder().build());
        return response.getNomesList();
    }

    @PostMapping("/cidade")
    public String cadastrarCidade(@RequestParam String nome, @RequestParam float temp) {
        NovaCidadeRequest request = NovaCidadeRequest.newBuilder()
                .setNome(nome)
                .setTemperaturaInicial(temp)
                .build();

        StatusResponse response = weatherStub.cadastrarCidade(request);
        return response.getMensagem();
    }

    @GetMapping("/previsao")
    public List<Float> getPrevisao(@RequestParam String cidade) {
        CidadeRequest request = CidadeRequest.newBuilder()
                .setNome(cidade)
                .build();

        PrevisaoResponse response = weatherStub.previsaoCincoDias(request);

        return response.getTemperaturasList();
    }

    @GetMapping("/estatisticas")
    public String getEstatisticas(@RequestParam String cidade) {
        CidadeRequest request = CidadeRequest.newBuilder().setNome(cidade).build();
        EstatisticasResponse res = weatherStub.estatisticasClimaticas(request);

        return String.format("Estatísticas para %s: Mínima: %.1f, Máxima: %.1f, Média: %.1f",
                cidade, res.getMinima(), res.getMaxima(), res.getMedia());
    }

}