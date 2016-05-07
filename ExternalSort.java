package externalSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

/* Trabalho elaborado por:
Carlos Magno Badu de Sousa Junior - 110820
José Carlos Rangel do Nascimento - 126082
Mayssa Laiane Nogueira da Silva - 123390
*/

public class ExternalSort {
	// Dividimos o arquivo em pequenos blocos. Se os blocos são
	// muito pequenos, criamos arquivos temporários.
	// Se eles são muito grandes, operamos o programa com muita memória.

	public static long calcularTamBloco(File arq_inicial)
	{
		long tam_arq = arq_inicial.length();    

		// Não queremos abrir mais que 1024 arquivos temporários, melhor 
		// rodar com memória cheia primeiro.

		final int MAX_TEM_PARQ = 1024;
		long tam_bloco = tam_arq / MAX_TEM_PARQ ;

		// E não queremos criar muitos arquivos temporários também.
		// Então ajustamos o tamanho do bloco para isso.

		long memoria_livre = Runtime.getRuntime().freeMemory();
		System.out.println(Runtime.getRuntime().freeMemory());
		if( tam_bloco < memoria_livre/2)
			tam_bloco = memoria_livre/2;
		else
			if(tam_bloco >= memoria_livre) 
				System.err.println("Programa rodando com memória cheia. ");

		return tam_bloco;
	}    

	// Esse método carrega o arquivo por blocos, ordena na mémoria,
	// escreve o resultado em um grupo de arquivos temporários e então
	// posteriormente usaremos o merge sort neles.

	public static List<File> carregarBloco(File arq, Comparator<String> cmp) throws IOException 
	{
		List<File> files = new ArrayList<File>();
		BufferedReader buffer_r = new BufferedReader(new FileReader(arq));
		long tam_bloco = calcularTamBloco(arq);

		try{
			List<String> tmplist =  new ArrayList<String>();
			String linha = "";

			try {
				while(linha != null) {

					long tam_bloco_atual = 0;
					while((tam_bloco_atual < tam_bloco) && ((linha = buffer_r.readLine()) != null)) //
					{
						tmplist.add(linha);
						tam_bloco_atual += linha.length();
					}

					files.add(salvarOrdenacao(tmplist,cmp));
					tmplist.clear();
				}
			} catch(EOFException eof) {
				if(tmplist.size()>0) {
					files.add(salvarOrdenacao(tmplist,cmp));
					tmplist.clear();
				}
			}
		} finally {
			buffer_r.close();
		}
		return files;
	}

	public static File salvarOrdenacao(List<String> tmplist, Comparator<String> cmp) throws IOException {
		Collections.sort(tmplist,cmp);
		File novotmparq = File.createTempFile("sortInBatch", "flatbuffer_r");
		novotmparq.deleteOnExit();
		BufferedWriter buffer_w = new BufferedWriter(new FileWriter(novotmparq));

		try {
			for(String r : tmplist) {
				buffer_w.write(r);
				buffer_w.newLine();
			}
		} finally {
			buffer_w.close();
		}
		return novotmparq;
	}

	public static int mergeSort(List<File> files, File arq_ordenado, final Comparator<String> cmp) throws IOException {				
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, 
			new Comparator<BinaryFileBuffer>() {
				public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
					return cmp.compare(i.registro(), j.registro());
				}
			}
		);

		for (File f : files) {
			BinaryFileBuffer bfb = new BinaryFileBuffer(f);
			pq.add(bfb);
		}

		BufferedWriter buffer_w = new BufferedWriter(new FileWriter(arq_ordenado));
		int qtd_linha = 0;

		try {
			while(pq.size()>0) {

				BinaryFileBuffer bfb = pq.poll();
				String r = bfb.estouro();
				buffer_w.write(r);
				buffer_w.newLine();
				++qtd_linha;

				if(bfb.vazio()) {
					bfb.buffer_r.close();
					bfb.arq_original.delete();
				} else
					pq.add(bfb);
			}
		} finally { 
			buffer_w.close();
			for(BinaryFileBuffer bfb : pq ) bfb.close();
		}
		return qtd_linha;
	}

	public static void main(String[] args) throws IOException {
		int i, op;
		final int QTD = 1075268817; // Esse quantidade de registros equivale a 6 GB. 1075268817

		Random rand = new Random();
		Scanner scan = new Scanner(System.in);
		
		String arq_inicial = "C:/Users/jose_/Desktop/texto.txt";
		String arq_ordenado = "C:/Users/jose_/workspace/teste/files/Arquivo Ordenado.txt";

		System.out.println("Infome uma opção: ");
		System.out.println("1 - Gerar arquivo");
		System.out.println("2 - Ler arquivo");
		op = scan.nextInt();
		scan.close();

		if (op == 1){ 
			try {
				FileWriter arq = new FileWriter(arq_inicial);
				PrintWriter gravarArq = new PrintWriter(arq);

				for (i = 0; i < QTD; i++)
					gravarArq.println(rand.nextInt(8999)+1000); // Gera apenas números randomicos de 32 bits (1000 a 9999).

				System.out.println("Arquivo gerado com sucesso!");
				arq.close();

			} catch (Exception e) {
				System.out.println(e);
			}
		}else{
			Comparator<String> comp = new Comparator<String>(){
				public int compare(String r1, String r2){
					return r1.compareTo(r2);
				}
			};

			List<File> lista = carregarBloco(new File(arq_inicial), comp) ;
			mergeSort(lista, new File(arq_ordenado), comp);

			System.out.println("Finalizado!");
			
		}
	}
}
