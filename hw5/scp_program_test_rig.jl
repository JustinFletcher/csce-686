
path_to_program_dir = "C:\\Users\\Justin Fletcher\\OneDrive\\afit\\CSCE686\\NPC Problems\\Software\\SCP\\SCP Solver 2006"
cd(path_to_program_dir)


function run_scp_program(input_file_name)
   open(`java -jar "SCP Solver 2006.jar" `, "w", STDOUT) do io

               println(io, input_file_name)
               println(io, "")
               println(io, "n")
               println(io, "")
               println(io, "n")
    end
end

run_scp_program("SampleInput.txt")

function construct_random_set_matrix(n_sets, n_cover_elements, density)

    edges = density*((n_sets*n_cover_elements))

    mask = randperm((n_sets*n_cover_elements))[1:edges]
    a = reshape([(i in mask) ? 1 : 0 for i in 1:(n_sets*n_cover_elements)],(n_cover_elements,n_sets))

    return(a)

end

a = construct_random_set_matrix(20, 10, 0.5)

size(a)[1]

{ 1 2 3 4 5 6 }
{
    ({ 1 2 6 },4)
    ({ 1 4 6 },7)
    ({ 1 2 },5)
    ({ 3 },8)
    ({ 4 5 },3)
    ({ 1 2 5 },2)
    ({ 4 5 6 },6)
    ({ 1 2 4 },5)
}


function set_matrix_to_input_file(a, filename, path_to_program_dir)
    f = open(path_to_program_dir*"\\"*filename, "w")

    num_elements = size(a)[1]
    num_sets = size(a)[2]

    write(f, "{")

    [write(f, " "*string(n)) for n in 1:num_elements]

    write(f, " }\n")
    write(f, "{\n")

    for set in 1:num_sets

        write(f, "\t(")
        write(f, "{")

        for covers_element in 1:num_elements
            if (a[covers_element,set]==1)
                write(f, " $covers_element")
            end
        end
        write(f, " }")
        cost=rand(1:100)
        write(f, ",$cost")
        write(f, ")\n")
    end

    write(f, "}")


    close(f)
end

set_matrix_to_input_file(a, "deleteme.txt", path_to_program_dir)

function run_scp_program_experiment(num_reps, n_sets_seq, n_elements_seq, density)

    runtime_matrix = zeros((num_reps,length(n_elements_seq),length(n_sets_seq)))
    for rep_num in 1:num_reps

        for n_elements in 1:length(n_elements_seq)
            for n_sets in 1:length(n_sets_seq)
                println("Number of Elements = ")

                # Construct a random set cover problem
                a = construct_random_set_matrix(n_sets, n_elements, density)

                set_matrix_to_input_file(a, "temp_input_file.txt", path_to_program_dir)

                tic()
                run_scp_program("temp_input_file.txt")
                runtime = toq()

                runtime_matrix[rep_num, n_elements, n_sets] = runtime

            end
        end

    end

    return(runtime_matrix)

end

##############################################
using PyPlot




function movingWindowAverage(inputVector, windowSize)

    movingAverageWindowVector = Float64[]

    for (windowMidIndex = 1:length(inputVector))

        windowStartIndex = maximum([minimum([windowMidIndex-floor(windowSize/2), (length(inputVector)-windowSize)]), 1])

        windowEndIndex = minimum([length(inputVector),(windowStartIndex+windowSize)])

        push!(movingAverageWindowVector, mean(inputVector[windowStartIndex:windowEndIndex]))

    end

  return(movingAverageWindowVector)

end



##############################################
num_reps = 5
n_sets_seq = [10:10:1000]
n_elements_seq = [10:5:100]

scp_runtime_mat = @time run_scp_program_experiment(num_reps, n_sets_seq, n_elements_seq, 0.30)

scp_runtime_mat_mean = slice(mean(scp_runtime_mat,1), 1,:,:)

# Show the 2d plot
subplot(2,3,(2,3))
imshow(scp_runtime_mat_mean,interpolation="none")
colorbar(orientation="horizontal")

subplot(2,3,(5,6))
scp_runtime_by_nsets_mat_mean = mean(scp_runtime_mat_mean ,1)
scp_runtime_by_nsets_mat_std = std(scp_runtime_mat_mean ,1)

plot(n_sets_seq, vec(scp_runtime_by_nsets_mat_mean), label="Output Calculation Time", color="blue")
errorbar(n_sets_seq, vec(scp_runtime_by_nsets_mat_mean), yerr=vec(scp_runtime_by_nsets_mat_std), fmt=".", alpha=0.7, color="blue")
title("Set-Wise Marginal Mean Running Time of the SCP Program")
xlabel(" \$\ n \$\ (Number of Sets)")
ylabel(" \$\ t \$\ \$\ (s) \$\ ")
legend(loc=2)

scp_runtime_by_nelemnts_mat_mean = mean(scp_runtime_mat_mean, 2)


subplot(2,3,1)
scp_runtime_by_nelements_mat_mean = mean(scp_runtime_mat_mean ,2)
scp_runtime_by_nelements_mat_std = std(scp_runtime_mat_mean ,2)

plot(n_elements_seq, vec(scp_runtime_by_nelements_mat_mean), label="Output Calculation Time", color="blue")
errorbar(n_elements_seq, vec(scp_runtime_by_nelements_mat_mean), yerr=vec(scp_runtime_by_nelements_mat_std), fmt=".", alpha=0.7, color="blue")
title("Cover-Element-Wise Marginal Mean Running Time of the SCP Program")
xlabel(" \$\ n \$\ (Number of Elemtns to Cover)")
ylabel(" \$\ t \$\ \$\ (s) \$\ ")
legend(loc=2)
