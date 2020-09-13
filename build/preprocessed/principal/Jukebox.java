package principal;

import java.io.IOException;
import java.util.Random;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.midlet.*;

public class Jukebox extends MIDlet implements CommandListener, ItemStateListener {
    private Display display;
    private Form frmPrincipal, frmLoop, frmReproducao, frmVolume;
    private TextField txtLoop;
    private List lstPacotes, lstHardRock, lstProgressive, lstMetal;
    private VolumeControl ctrVolume;
    private Gauge gauVolume;
    private Command cmdSelecionar, cmdVoltar, cmdSair, cmdAbrir, cmdReproduzir, cmdParar, cmdVolume;
    private Image imgCapa, imgPrincipal, iHard, iProg, iMetal;
    private Alert alerta;
    private Ticker tkrOuvindo, tkrFrase;
    private Player player;
    private Random random;
    private String nomeMusicaSelecionada = null;
    private int totalMusicas;
    private int codigoMusicaSelecionada;
    private String[] arrayMusicas;
    private int loops = 1;
    private boolean tocando = false;
    private boolean aleatoria = false;

    public void startApp() {
        try {
            criarIcones();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        frmPrincipal = new Form("Rock 'n' Roll Jukebox");
        frmLoop = new Form("");
        frmReproducao = new Form("");
        frmVolume = new Form("");

        cmdSelecionar = new Command("Selecionar", Command.OK, 0);
        cmdVoltar = new Command("Voltar", Command.BACK, 0);
        cmdSair = new Command("Sair", Command.EXIT, 0);
        cmdAbrir = new Command("Abrir", Command.OK, 0);
        cmdReproduzir = new Command("Reproduzir", Command.OK, 0);
        cmdParar = new Command("Parar", Command.OK, 0);
        cmdVolume = new Command("Volume", Command.OK, 0);

        lstPacotes = new List("Selecione o pacote", List.IMPLICIT);
        lstHardRock = new List("Hard Rock", List.IMPLICIT);
        lstProgressive = new List("Prog/Psychedelic Rock", List.IMPLICIT);
        lstMetal = new List("Metal", List.IMPLICIT);

        alerta = new Alert("Informação necessária!", "Digite a quantidade de vezes que a música irá reproduzir!", null, AlertType.ERROR);
        alerta.setTimeout(3000);
        tkrOuvindo = new Ticker("");
        tkrFrase = new Ticker("");
        txtLoop = new TextField("Reproduzir quantas vezes?", null, 2, TextField.NUMERIC);
        gauVolume = new Gauge("Volume", true, 20, 10);
        random = new Random();

        try {
            imgPrincipal = Image.createImage("/principal/img.jpg");
            frmPrincipal.append(imgPrincipal);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        frmPrincipal.addCommand(cmdSair);
        frmPrincipal.addCommand(cmdAbrir);
        frmPrincipal.setCommandListener(this);

        frmLoop.append(txtLoop);
        frmLoop.addCommand(cmdVoltar);
        frmLoop.addCommand(cmdReproduzir);
        frmLoop.setCommandListener(this);

        frmReproducao.setTicker(tkrFrase);
        frmReproducao.addCommand(cmdVoltar);
        frmReproducao.addCommand(cmdParar);
        frmReproducao.addCommand(cmdVolume);
        frmReproducao.setCommandListener(this);

        frmVolume.append(gauVolume);
        frmVolume.addCommand(cmdVoltar);
        frmVolume.setCommandListener(this);
        frmVolume.setItemStateListener(this);

        lstPacotes.append(lstHardRock.getTitle(), iHard);
        lstPacotes.append(lstProgressive.getTitle(), iProg);
        lstPacotes.append(lstMetal.getTitle(), iMetal);
        lstPacotes.append("Reproduzir música aleatória", null);
        lstPacotes.addCommand(cmdVoltar);
        lstPacotes.addCommand(cmdSelecionar);
        lstPacotes.setCommandListener(this);

        lstHardRock.append("Led Zeppelin - Whole Lotta Love", null);
        lstHardRock.append("Black Sabbath - Into The Void", null);
        lstHardRock.append("Rainbow - Stargazer", null);
        lstHardRock.append("Deep Purple - Burn", null);
        lstHardRock.addCommand(cmdVoltar);
        lstHardRock.addCommand(cmdSelecionar);
        lstHardRock.setCommandListener(this);

        lstProgressive.append("Rush - The Spirit Of Radio", null);
        lstProgressive.append("Pink Floyd - Have a Cigar", null);
        lstProgressive.append("Jimi Hendrix - Purple Haze", null);
        lstProgressive.append("Yes - Heart Of The Sunrise", null);
        lstProgressive.addCommand(cmdVoltar);
        lstProgressive.addCommand(cmdSelecionar);
        lstProgressive.setCommandListener(this);

        lstMetal.append("Tool - Jambi", null);
        lstMetal.append("Megadeth - Take No Prisoners", null);
        lstMetal.append("System Of A Down - Tentative", null);
        lstMetal.append("Pantera - Walk", null);
        lstMetal.addCommand(cmdVoltar);
        lstMetal.addCommand(cmdSelecionar);
        lstMetal.setCommandListener(this);
        
        pegarNomeMusicas();
        display = Display.getDisplay(this);
        display.setCurrent(frmPrincipal);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }

    public void commandAction(Command c, Displayable d) {
        // Comando SAIR
        if (c.equals(cmdSair)) {
            destroyApp(true);
        }

        // Comando ABRIR
        if (c.equals(cmdAbrir)) {
            display.setCurrent(lstPacotes);
        }

        /* Comando VOLTAR:
         * Se estiver no form de reprodução ou no de Loops, zera o TextField de Loops, apaga
         * todos os itens (capa do álbum) do form de reprodução e chama o método que verifica
         * para qual tela o display deve voltar.
         */
        if (c.equals(cmdVoltar)) {
            if (d.equals(lstPacotes)) {
                display.setCurrent(frmPrincipal);
                if (tocando) {
                    frmPrincipal.setTicker(tkrOuvindo);
                } else {
                    frmPrincipal.setTicker(null);
                }
            }

            if (d.equals(lstHardRock) || d.equals(lstProgressive) || d.equals(lstMetal)) {
                display.setCurrent(lstPacotes);
            }

            if (d.equals(frmReproducao) || d.equals(frmLoop)) {
                frmReproducao.deleteAll();
                txtLoop.setString(null);
                if (aleatoria) {
                    display.setCurrent(lstPacotes);
                } else {
                    voltar(lstHardRock);
                    voltar(lstProgressive);
                    voltar(lstMetal);
                }
                frmReproducao.removeCommand(cmdReproduzir);
                frmReproducao.removeCommand(cmdVolume);
                frmReproducao.addCommand(cmdParar);
                frmReproducao.addCommand(cmdVolume);
            }

            if (d.equals(frmVolume)) {
                display.setCurrent(frmReproducao);
            }
        }

        /* Comando SELECIONAR:
         * Verifica para qual tela o display deve ir e seta o título do form de Loops com o
         * valor da variável nomeMusica.
         */
        if (c.equals(cmdSelecionar)) {
            if (d.equals(lstPacotes)) {
                switch (lstPacotes.getSelectedIndex()) {
                    case 0:
                        display.setCurrent(lstHardRock);
                        break;
                    case 1:
                        display.setCurrent(lstProgressive);
                        break;
                    case 2:
                        display.setCurrent(lstMetal);
                        break;
                    case 3:
                        musicaAleatoria();
                        break;
                }
            }

            try {
                verificaExecucao(d, lstHardRock);
                verificaExecucao(d, lstProgressive);
                verificaExecucao(d, lstMetal);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Comando REPRODUZIR
        if (c.equals(cmdReproduzir)) {
            if (d.equals(frmLoop)) {
                if (txtLoop.getString().length() == 0 || Integer.parseInt(txtLoop.getString()) <= 0) {
                    display.setCurrent(alerta);
                } else {
                    try {
                        loops = Integer.parseInt(txtLoop.getString());
                        aleatoria = false; // Se passar pelo form de Loops quer dizer que não foi uma execução aleatória.
                        reproduzir(loops);
                    } catch (MediaException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            if (d.equals(frmReproducao)) {
                frmReproducao.deleteAll();
                frmReproducao.removeCommand(cmdReproduzir);
                frmReproducao.removeCommand(cmdVolume);
                frmReproducao.addCommand(cmdParar);
                frmReproducao.addCommand(cmdVolume);
                try {
                    reproduzir(loops);
                } catch (MediaException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Comando PARAR
        if (c.equals(cmdParar)) {
            try {
                pararMusica();
            } catch (MediaException ex) {
                ex.printStackTrace();
            }
            frmReproducao.removeCommand(cmdParar);
            frmReproducao.removeCommand(cmdVolume);
            frmReproducao.addCommand(cmdReproduzir);
            frmReproducao.addCommand(cmdVolume);
        }

        // Comando VOLUME
        if (c.equals(cmdVolume)) {
            frmVolume.setTitle(nomeMusicaSelecionada);
            display.setCurrent(frmVolume);
        }
    }

    public void itemStateChanged(Item item) {
        if (item.equals(gauVolume)) {
            ctrVolume.setLevel((gauVolume.getValue() * 5));
        }
    }

    /* Chama o método para parar a música, cria o Player com a música que foi selecionada (cujo nome
     * está na variável nomeMusica), adiciona um controle de volume no Player, e seta o nível de volume
     * com o valor da variável volume (para fazer com que sempre que o usuário altera o volume da música
     * atual, a próxima música iniciará com o mesmo volume).
     */
    public void reproduzir(int loops) throws MediaException, IOException {
        pararMusica();
        codigoMusicaSelecionada = pegarCodigoMusicaSelecionada();
        player = Manager.createPlayer(getClass().getResourceAsStream("/musicas/" + nomeMusicaSelecionada + ".mp3"), "audio/mp3");
        player.setLoopCount(loops);
        player.prefetch();
        player.realize();
        ctrVolume = (VolumeControl) player.getControl("VolumeControl");
        ctrVolume.setLevel((gauVolume.getValue() * 5));
        player.start();
        tocando = true;
        setarCapa();
        tkrOuvindo.setString("Ouvindo agora: " + nomeMusicaSelecionada);
        tkrFrase.setString(infoAlbuns());
        frmReproducao.setTitle(nomeMusicaSelecionada);
        frmReproducao.append(imgCapa);
        display.setCurrent(frmReproducao);
    }

    public void pararMusica() throws MediaException {
        if (tocando) {
            player.close();
            tocando = false;
        }
    }
    
    // Preenche o Array "arrayMusicas" com o nome de todas as músicas.
    public void pegarNomeMusicas() {
        totalMusicas = lstHardRock.size() + lstProgressive.size() + lstMetal.size();
        arrayMusicas = new String[totalMusicas];
        
        int i = 0;
        for (int h = 0; h < lstHardRock.size(); h++, i++) {
            arrayMusicas[i] = lstHardRock.getString(h);
        }

        for (int p = 0; p < lstProgressive.size(); p++, i++) {
            arrayMusicas[i] = lstProgressive.getString(p);
        }

        for (int m = 0; m < lstMetal.size(); m++, i++) {
            arrayMusicas[i] = lstMetal.getString(m);
        }
    }
    
    public int pegarCodigoMusicaSelecionada() {
        for (int i=0; i<arrayMusicas.length; i++) {
            if (arrayMusicas[i].equals(nomeMusicaSelecionada)) {
                return i;
            }
        }
        return -1;
    }
    
    public String infoAlbuns() {
        String info = null;
        switch (codigoMusicaSelecionada) {
            case 0: info = "Led Zeppelin II, 1969"; break;
            case 1: info = "Master Of Reality, 1970"; break;
            case 2: info = "Rising, 1976"; break;
            case 3: info = "Burn, 1974"; break;
            case 4: info = "Permanent Waves, 1980"; break;
            case 5: info = "Wish You Were Here, 1975"; break;
            case 6: info = "Are You Experienced, 1967"; break;
            case 7: info = "Fragile, 1971"; break;
            case 8: info = "10000 Days, 2006"; break;
            case 9: info = "Rust In Peace, 1990"; break;
            case 10: info = "Hypnotize, 2005"; break;
            case 11: info = "Vulgar Display Of Power, 1992"; break;
        }
        return info;
    }
    
    public void musicaAleatoria() {
        pegarNomeMusicas();
        codigoMusicaSelecionada = random.nextInt(totalMusicas);
        nomeMusicaSelecionada = arrayMusicas[codigoMusicaSelecionada];
        aleatoria = true;

        try {
            reproduzir(loops);
        } catch (MediaException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /* Alimenta a variável nomeMusica e verifica se a música selecionada está sendo executada no momento.
     * Se estiver, seta novamente a capa do álbum no form de reprodução (pois no comando Voltar
     * os elementos do form são apagados) e mostra o form. Se não estiver executando, é pedida a
     * quantidade de Loops para executar a nova música.
     */
    public void verificaExecucao(Displayable d, List lista) throws IOException {
        if (d.equals(lista)) {
            nomeMusicaSelecionada = lista.getString(lista.getSelectedIndex());
            if (frmReproducao.getTitle().equals(nomeMusicaSelecionada) && tocando == true) {
                setarCapa();
                frmReproducao.append(imgCapa);
                display.setCurrent(frmReproducao);
            } else {
                display.setCurrent(frmLoop);
            }
            frmLoop.setTitle(nomeMusicaSelecionada);
        }
    }

    public void setarCapa() throws IOException {
        imgCapa = Image.createImage("/capas/" + nomeMusicaSelecionada + ".jpg");
    }

    public void criarIcones() throws IOException {
        iHard = Image.createImage("/icones/hard.png");
        iProg = Image.createImage("/icones/prog.png");
        iMetal = Image.createImage("/icones/metal.png");
    }

    // Volta para a tela do pacote que a música pertence (Hard/Prog/Metal).
    public void voltar(List lista) {
        for (int i = 0; i < lista.size(); i++) {
            if (frmReproducao.getTitle().equals(lista.getString(i))
                    || frmLoop.getTitle().equals(lista.getString(i))) {
                display.setCurrent(lista);
            }
        }
    }

    public String fraseAleatoria() {
        String[] frases = new String[6];
        frases[0] = "\"O cara que disse que dinheiro não compra felicidade, não sabia onde fazer compras.\" – "
                + "David Lee Roth (Van Halen)";
        frases[1] = "\"Ninguém gosta do Sabbath, a não ser o público.\" – Tony Iommi (Black Sabbath)";
        frases[2] = "\"Se as portas da percepção forem abertas, as coisas irão surgir como realmente são: infinitas.\" – "
                + "Jim Morrison (The Doors)";
        frases[3] = "\"O Rock 'n' Roll retarda o envelhecimento!\" – Bruce Springsteen";
        frases[4] = "\"A mente é como um para-quedas. Só funciona se abri-lo.\" – Frank Zappa";
        frases[5] = "\"Rock 'n' Roll não se aprende nem se ensina.\" – Raul Seixas";

        return frases[random.nextInt(6)];
    }
}