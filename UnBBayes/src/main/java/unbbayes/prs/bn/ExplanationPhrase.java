package unbbayes.prs.bn;

/**
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.0
 */
public class ExplanationPhrase
{   public static final int TRIGGER_EVIDENCE_TYPE = 0;
    public static final int COMPLEMENTARY_EVIDENCE_TYPE = 2;
    public static final int NA_EVIDENCE_TYPE = 4;
    public static final int NECESSARY_EVIDENCE_TYPE = 1;
    public static final int EXCLUSIVE_EVIDENCE_TYPE = 3;
    private String strPhrase, strNode;
    private int evidenceType;

    public ExplanationPhrase()
    {   this.strPhrase = "";
        this.strNode = "";
        this.evidenceType = NA_EVIDENCE_TYPE;
    }

    public ExplanationPhrase(String strPhrase, String strNode,int evidenceType)
    {   this.strPhrase = strPhrase;
        this.strNode = strNode;
        this.evidenceType = evidenceType;
    }

    /** Altera o tipo de evidência do nó de explicação.
    *  Os tipos de evidência podem ser:
    *  -   TRIGGER_EVIDENCE_TYPE : nó trigger
    *  -   COMPLEMENTARY_EVIDENCE_TYPE : nó complementar
    *  -   NA_EVIDENCE_TYPE : nó na
    *  -   NECESSARY_EVIDENCE_TYPE : nó necessário
    *  -   EXCLUSIVE_EVIDENCE_TYPE : nó exclusivo
    *  @param evidenceType Tipo de evidência
    */
    public void setEvidenceType(int evidenceType)
    {   if ((evidenceType > -1) && (evidenceType < 5))
        {   this.evidenceType = evidenceType;
        }
    }

    /** Retorna o tipo de evidência de um nó de explicação.
    *  @return Tipo de evidência de um nó.
    */
    public int getEvidenceType()
    {   return evidenceType;
    }

    public void setPhrase(String strPhrase)
    {   this.strPhrase = strPhrase;
    }

    public String getPhrase()
    {   return strPhrase;
    }

    public void setNode(String strNode)
    {   this.strNode = strNode;
    }

    public String getNode()
    {   return strNode;
    }
}