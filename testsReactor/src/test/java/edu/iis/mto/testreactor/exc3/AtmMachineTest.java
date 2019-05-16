package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

public class AtmMachineTest {

    private CardProviderService cardService;
    private BankService bankService;
    private MoneyDepot moneyDepot;

    @Before
    public void setUp() {
        cardService = mock(CardProviderService.class);
        bankService = mock(BankService.class);
        moneyDepot = mock(MoneyDepot.class);
    }

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void withdraw_zero_throws() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = Money.builder().withAmount(0).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("").withPinNumber(123).build();

        atmMachine.withdraw(money, card);
    }
}
