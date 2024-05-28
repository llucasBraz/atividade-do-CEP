import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String dadosArquivo = "dados.txt";
        String nome, cpf, cep = "0", endereco = "0";
        double salario, desconto;
        int dependentes;

        List<String[]> pessoas = new ArrayList<>(); 

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(new FileInputStream(dadosArquivo), StandardCharsets.UTF_8))){
            String linha;
            while ((linha = leitor.readLine()) != null) {
                String[] pessoa = linha.split(", ");
                pessoas.add(pessoa);
            }
        } catch (IOException ex) {

        }

        System.out.print("Digite seu nome: ");
        nome = scanner.nextLine();

        System.out.print("Digite seu salário: ");
        salario = scanner.nextDouble();

        System.out.print("Digite o desconto: ");
        desconto = scanner.nextDouble();

        scanner.nextLine();

        System.out.print("Digite o número de dependentes: ");
        dependentes = scanner.nextInt();

        scanner.nextLine();

        double valorDependentes = dependentes * 189.59;
        double baseImposto = salario - desconto - valorDependentes;
        double valorImposto;
        double salarioLiquido;

        if (baseImposto <= 2259.20) {
            valorImposto = 0;
        } else if (baseImposto >= 2259.21 && baseImposto <= 2826.65) {
            valorImposto = baseImposto * 0.075 - 169.44;
        } else if (baseImposto >= 2826.66 && baseImposto <= 3751.05) {
            valorImposto = baseImposto * 0.15 - 381.44;
        } else if (baseImposto >= 3751.06 && baseImposto <= 4664.68) {
            valorImposto = baseImposto * 0.225 - 662.77;
        } else {
            valorImposto = baseImposto * 0.275 - 896.00;
        }

        DecimalFormat formato = new DecimalFormat("#,##00.00");

        if (valorImposto == 0) {
            salarioLiquido = salario - desconto;
            System.out.println("\nSalario Líquido: " + formato.format(salarioLiquido) + "\nIsento de imposto de renda!\n");
        } else {
            salarioLiquido = salario - desconto - valorImposto;
            System.out.println("\nSalario Líquido: " + formato.format(salarioLiquido) + "\nImposto de renda: " + formato.format(valorImposto) + "\n");
        }

        System.out.print("Digite seu CPF: ");
        cpf = scanner.nextLine();

        if (cpf.length() != 11) {
            do {
                System.out.print("CPF inválido, digite novamente: ");
                cpf = scanner.nextLine();
            } while (cpf.length() != 11);
        }

        System.out.print("Digite o CEP: ");

        boolean cepValido = false;
        while (!cepValido) {
            cep = scanner.nextLine();

            try {
                URL url = new URL("https://viacep.com.br/ws/" + cep + "/json"); 
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection(); 
                conexao.setRequestMethod("GET"); 

                BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8)); 
                String linha;
                StringBuilder resposta = new StringBuilder();

                while((linha = leitor.readLine()) != null) {
                    resposta.append(linha); 
                }
                leitor.close();

                JSONObject respostaJSON = new JSONObject(resposta.toString()); 

                if (respostaJSON.has("erro")){
                    System.out.print("CEP inválido, digite novamente: ");
                } else {
                    String logradouro = respostaJSON.getString("logradouro");
                    String bairro = respostaJSON.getString("bairro");
                    String cidade = respostaJSON.getString("localidade");
                    String estado = respostaJSON.getString("uf");

                    endereco = logradouro + " - " + bairro + " " + cidade + " - " + estado;
                    System.out.println("Endereço: " + endereco);
                    cepValido = true;
                }

            } catch (Exception e) {
                System.out.print("CEP inválido, digite novamente: ");
            }
        }

        String salarioString = formato.format(salario);
        String liquidoString = formato.format(salarioLiquido);
        String dependentesString = String.valueOf(dependentes);

        boolean cpfExistente = false;
        for (String[] pessoa : pessoas) {
            if (pessoa.length >= 2 && pessoa[1].equals(cpf)) {
                pessoa[0] = nome;
                pessoa[2] = liquidoString;
                pessoa[3] = dependentesString;
                pessoa[4] = salarioString;
                pessoa[5] = cep;
                pessoa[6] = endereco;
                cpfExistente = true;
                break;
            }
        }

        if (!cpfExistente){
            pessoas.add(new String[]{nome, cpf, liquidoString, dependentesString, salarioString, cep, endereco}); 
        }

        try (BufferedWriter escritor = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dadosArquivo), StandardCharsets.UTF_8))){
            for (String[] pessoa : pessoas) {
                for (int i = 0; i < pessoa.length; i++) {
                    escritor.write(pessoa[i]);
                    if (i < pessoa.length - 1) {
                        escritor.write(", ");
                    } else {
                        escritor.write("\n");
                    }
                }
            }
        } catch(IOException e) {
            System.err.println("Ocorreu um erro");
        }

        scanner.close();
    }
}
