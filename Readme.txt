Sistema de Locação de Veículos "Rota Segura" (TP1). 
Este trabalho foi desenvolvido pelo aluno Marcelo Junior para a 
disciplina de Programação Orientada a Objetos (POO), utilizando a 
IDE Apache NetBeans e a linguagem Java (JDK 21+).

O Rota Segura é um sistema para gerenciamento de locação de veículos 
desenvolvido em Java. O projeto tem como objetivo substituir controles manuais 
por uma aplicação orientada a objetos resiliente e escalável, aplicando os princípios 
fundamentais da Programação Orientada a Objetos (POO).

Quanto aos conceitos de POO aplicados, o primeiro deles é o Encapsulamento, onde os 
atributos possuem modificadores de acesso restritivos (private) e métodos de acesso 
(getters e setters) contendo validações para garantir a integridade dos dados, como 
a validação de CPF com 11 dígitos, tamanho mínimo de placa e valores positivos. 

O segundo conceito é a Herança e Abstração, através da classe abstrata Veiculo, 
que define o contrato e os atributos genéricos da frota, e das classes especializadas 
VeiculoPopular, VeiculoSedan e VeiculoSUV, que estendem a classe base. O terceiro conceito 
é o Polimorfismo, evidenciado pela implementação específica dos métodos abstratos calcularValorDiaria() 
e calcularSeguro() nas subclasses, além do uso da interface Imprimivel para padronizar a geração de 
comprovantes e relatórios. Em quarto lugar, temos os Tipos Genéricos (Generics), representados pela classe 
RepositorioGenerico, responsável por gerenciar coleções de qualquer tipo de entidade do sistema. O quinto conceito 
é o Tratamento de Exceções e Resiliência, com a implementação de exceções personalizadas (VeiculoIndisponivelException
e DadosInvalidosException) para o tratamento de regras de negócio e prevenção de falhas em tempo de execução. 
Por fim, o sexto conceito é a Persistência de Dados, realizada através da gravação automática dos comprovantes dos 
contratos emitidos em arquivos de texto (.txt).



classDiagram
    class Imprimivel {
        <<interface>>
        +gerarComprovante() String
    }

    class Veiculo {
        <<abstract>>
        -String placa
        -String marca
        -String modelo
        -int ano
        -double valorBaseDiaria
        -boolean disponivel
        +calcularValorDiaria()* double
        +calcularSeguro(int dias)* double
    }

    class VeiculoPopular {
        -boolean possuiArCondicionado
        +calcularValorDiaria() double
        +calcularSeguro(int dias) double
    }

    class VeiculoSedan {
        -double capacidadePortaMalasLitros
        +calcularValorDiaria() double
        +calcularSeguro(int dias) double
    }

    class VeiculoSUV {
        -boolean tracao4x4
        +calcularValorDiaria() double
        +calcularSeguro(int dias) double
    }

    class Cliente {
        -String cpf
        -String nome
        -String cnh
    }

    class Contrato {
        -int id
        -Cliente cliente
        -Veiculo veiculo
        -LocalDate dataInicio
        -LocalDate dataFimPrevista
        -boolean ativo
        +calcularValorTotal() double
        +fecharContrato(LocalDate dataDevolucao)
    }

    class RepositorioGenerico~T~ {
        -List~T~ elementos
        +adicionar(T elemento)
        +remover(T elemento) boolean
        +listarTodos() List~T~
    }

    Imprimivel <|.. Veiculo
    Imprimivel <|.. Cliente
    Imprimivel <|.. Contrato
    Veiculo <|-- VeiculoPopular
    Veiculo <|-- VeiculoSedan
    Veiculo <|-- VeiculoSUV
    Contrato --> Cliente
    Contrato --> Veiculo