package bootcamp;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;    
import static net.corda.core.contracts.ContractsDSL.requireThat;

import java.util.List;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class TokenContract implements Contract {
    public static String ID = "bootcamp.TokenContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        
        CommandWithParties<TokenContract.Commands> command =
                requireSingleCommand(tx.getCommands(), TokenContract.Commands.class);

        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();

        //if this is issue command
        if (command.getValue() instanceof TokenContract.Commands.Issue) {
            //rules for transaction that carries the issue command
            requireThat( req -> {
                //no inputs
                req.using("Transaction must have no inputs", inputs.isEmpty());
                //one output
                req.using("Transaction must have one output", outputs.size() == 1);
                //output must be a TokenState
                req.using("Output must be a TokenState object",
                        outputs.get(0) instanceof TokenState);
                //2 signers
                TokenState output = (TokenState) outputs.get(0);
                req.using("Issuer must be one of the required signer",
                        command.getSigners().contains(output.getIssuer().getOwningKey()));
                req.using("Owner must be one of the required signer",
                        command.getSigners().contains(output.getOwner().getOwningKey()));
                //amount > 0
                req.using("Amount must be positive", output.getAmount() > 0);

                return null;

            });

        //else if this is a transfer command    
        //} else if (command.getValue() instanceof TokenContract.Commands.Transfer) {

        } else {
            throw new IllegalArgumentException("Unknown command");
        }




    }


    public interface Commands extends CommandData {
        class Issue implements Commands { }
        class Transfer implements Commands { }
    }
}
